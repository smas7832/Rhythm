package chromahub.rhythm.app.util

import android.util.Log

/**
 * Audio quality categorization utility based on sample rate, bitrate, bit depth, and codec
 * 
 * Quality Levels (Based on industry standards):
 * - Lossy Compressed: MP3, AAC, OGG (128-320 kbps, 44.1 kHz, 16-bit after decoding)
 * - CD Quality (Lossless): FLAC, ALAC, WAV (~1,411 kbps, 44.1 kHz, 16-bit)
 * - Hi-Res Lossless: FLAC/ALAC (2,000-4,600 kbps, 48-96 kHz, 24-bit)
 * - Studio Master: FLAC/ALAC (4,600-9,200+ kbps, 192 kHz, 24-bit)
 * - Dolby Digital (Lossy): AC-3, E-AC-3 (192-640 kbps, 48 kHz, 16-bit compressed)
 * - Dolby TrueHD/Atmos (Lossless): (4,000-18,000 kbps, 48-192 kHz, 24-bit)
 * - DTS-HD Master Audio (Lossless): (4,000-18,000 kbps, 48-192 kHz, 24-bit)
 */
object AudioQualityDetector {
    private const val TAG = "AudioQualityDetector"

    /**
     * Represents the audio quality tier
     */
    enum class QualityType {
        LOSSY_COMPRESSED,      // MP3, AAC, OGG (128-320 kbps)
        CD_QUALITY_LOSSLESS,   // FLAC, ALAC 16/44.1
        HI_RES_LOSSLESS,       // FLAC, ALAC 24/48+ kHz
        HI_RES_STUDIO_MASTER,  // FLAC, ALAC 24/192 kHz
        LOSSLESS_SURROUND,     // e.g., FLAC 5.1
        DOLBY_LOSSY_SURROUND,  // AC-3, E-AC-3
        DOLBY_LOSSLESS,        // TrueHD, Atmos
        DTS_SURROUND,          // DTS variants
        UNKNOWN
    }

    /**
     * Detailed audio quality information
     */
    data class AudioQuality(
        val qualityType: QualityType,
        val qualityLabel: String,        // e.g., "Hi-Res Lossless", "CD Quality"
        val qualityDescription: String,  // e.g., "24-bit/96 kHz Studio Master"
        val isLossless: Boolean,
        val isHiRes: Boolean,
        val isDolby: Boolean,
        val isDTS: Boolean,
        val bitDepthEstimate: Int,       // 16 or 24 bit (or 0 if unknown)
        val category: String             // "Lossless", "Lossy", "Surround"
    )

    /**
     * Determine audio quality based on codec, sample rate, bitrate, and bit depth
     * 
     * @param codec Audio codec (e.g., "FLAC", "MP3", "AAC", "AC-3")
     * @param sampleRateHz Sample rate in Hz (e.g., 44100, 48000, 96000)
     * @param bitrateKbps Bitrate in kbps (e.g., 320, 1411, 4608)
     * @param bitDepth Bit depth if known (16, 24, or 0 if unknown)
     * @param channelCount Number of audio channels (1=mono, 2=stereo, 6=5.1, etc.)
     * @return AudioQuality with detailed quality information
     */
    fun detectQuality(
        codec: String,
        sampleRateHz: Int,
        bitrateKbps: Int,
        bitDepth: Int = 0,
        channelCount: Int = 2
    ): AudioQuality {
        val normalizedCodec = codec.uppercase().trim()
        val sampleRateKhz = sampleRateHz / 1000.0

        // Determine if codec is inherently lossless
        val isLosslessCodec = normalizedCodec in listOf(
            "FLAC", "ALAC", "WAV", "PCM", "APE", "DSD", "AIFF", "WMA LOSSLESS",
            "TRUEHD", "MLP", "DTS-HD", "DTS-HD MA", "APPLE LOSSLESS", "FLAC LOSSLESS"
        ) || normalizedCodec.contains("LOSSLESS")
        
        // Additional heuristic: Low bitrate for 44.1kHz can indicate lossless compression
        // CD quality uncompressed = 1411 kbps, but lossless can compress to 200-900 kbps
        // However, AAC at 288kbps would typically be for high quality lossy
        // ALAC/FLAC can be 200-1500 kbps depending on compression
        val isLikelyLosslessFromBitrate = !isLosslessCodec && 
                                          sampleRateHz in 44000..48000 && 
                                          bitrateKbps in 200..1500 &&
                                          !normalizedCodec.contains("MP3") &&
                                          !normalizedCodec.contains("OGG") &&
                                          !normalizedCodec.contains("OPUS") &&
                                          !normalizedCodec.contains("VORBIS")
        // Note: Not excluding AAC here because M4A containers can have ALAC but report as AAC
        
        val finalIsLossless = isLosslessCodec || isLikelyLosslessFromBitrate
        
        Log.d(TAG, "Codec detection: original='$codec', normalized='$normalizedCodec', " +
                "isLosslessCodec=$isLosslessCodec, likelyLossless=$isLikelyLosslessFromBitrate, " +
                "final=$finalIsLossless")

        // Detect Dolby variants
        val isDolbyCodec = normalizedCodec in listOf(
            "AC-3", "AC3", "E-AC-3", "EAC3", "DOLBY DIGITAL", "DOLBY DIGITAL PLUS",
            "TRUEHD", "DOLBY TRUEHD", "ATMOS", "DOLBY ATMOS", "MLP"
        )

        // Detect DTS variants
        val isDTSCodec = normalizedCodec.contains("DTS")

        // Estimate bit depth if not provided
        val estimatedBitDepth = when {
            bitDepth > 0 -> {
                Log.d(TAG, "Using provided bit depth: $bitDepth-bit")
                bitDepth
            }
            // Use bitrate-based calculation for lossless with improved thresholds
            finalIsLossless && bitrateKbps > 0 && sampleRateHz > 0 && channelCount > 0 -> {
                val calculated = (bitrateKbps * 1000) / (sampleRateHz * channelCount)
                // Reference calculations:
                // CD Quality: 44.1kHz/16-bit/stereo = 1,411 kbps → 16 bits/sample
                //   (1411 * 1000) / (44100 * 2) = 16.0
                // Hi-Res: 96kHz/24-bit/stereo = 4,608 kbps → 24 bits/sample
                //   (4608 * 1000) / (96000 * 2) = 24.0
                val result = when {
                    calculated >= 20 -> 24  // 24-bit (20-32 bits/sample, allowing for compression variation)
                    calculated >= 14 -> 16  // 16-bit (14-19 bits/sample)
                    calculated >= 10 -> 16  // 16-bit with high compression
                    else -> 16              // Default to 16 for lower bitrates
                }
                Log.d(TAG, "Bit depth from bitrate: codec=$normalizedCodec, " +
                        "bitrate=${bitrateKbps}kbps, sampleRate=${sampleRateHz}Hz, " +
                        "channels=$channelCount, calculated=$calculated bits/sample → $result-bit")
                result
            }
            // Fallback to sample rate heuristics for lossless codecs
            finalIsLossless && sampleRateHz >= 48000 -> {
                Log.d(TAG, "Bit depth from sample rate: ${sampleRateHz}Hz → 24-bit (Hi-Res assumed)")
                24
            }
            finalIsLossless -> {
                Log.d(TAG, "Bit depth default for lossless: 16-bit")
                16
            }
            else -> 16                    // Default assumption for lossy
        }

        // Determine quality type and details
        Log.d(TAG, "Quality detection: codec=$normalizedCodec, sampleRate=${sampleRateHz}Hz, " +
                "bitrate=${bitrateKbps}kbps, bitDepth=$estimatedBitDepth-bit, " +
                "channels=$channelCount, isLossless=$finalIsLossless")
        
        return when {
            // Dolby TrueHD / Atmos (Lossless surround)
            normalizedCodec in listOf("TRUEHD", "DOLBY TRUEHD", "ATMOS", "DOLBY ATMOS", "MLP") -> {
                AudioQuality(
                    qualityType = QualityType.DOLBY_LOSSLESS,
                    qualityLabel = if (normalizedCodec.contains("ATMOS")) "Dolby Atmos" else "Dolby TrueHD",
                    qualityDescription = "${estimatedBitDepth}-bit / ${sampleRateKhz.toInt()} kHz Lossless Surround",
                    isLossless = true,
                    isHiRes = true,
                    isDolby = true,
                    isDTS = false,
                    bitDepthEstimate = estimatedBitDepth,
                    category = "Lossless Surround"
                )
            }

            // Dolby Digital (AC-3, E-AC-3) - Lossy surround
            normalizedCodec in listOf("AC-3", "AC3", "E-AC-3", "EAC3", "DOLBY DIGITAL", "DOLBY DIGITAL PLUS") -> {
                val label = when {
                    normalizedCodec.contains("E-AC-3") || normalizedCodec.contains("EAC3") ||
                    normalizedCodec.contains("PLUS") -> "Dolby Digital Plus"
                    else -> "Dolby Digital"
                }
                AudioQuality(
                    qualityType = QualityType.DOLBY_LOSSY_SURROUND,
                    qualityLabel = label,
                    qualityDescription = "$bitrateKbps kbps / ${sampleRateKhz.toInt()} kHz Surround",
                    isLossless = false,
                    isHiRes = false,
                    isDolby = true,
                    isDTS = false,
                    bitDepthEstimate = 16,
                    category = "Lossy Surround"
                )
            }

            // DTS (various forms)
            isDTSCodec -> {
                val isLosslessDTS = normalizedCodec.contains("HD") || normalizedCodec.contains("MA")
                AudioQuality(
                    qualityType = QualityType.DTS_SURROUND,
                    qualityLabel = if (isLosslessDTS) "DTS-HD Master Audio" else "DTS",
                    qualityDescription = if (isLosslessDTS)
                        "${estimatedBitDepth}-bit / ${sampleRateKhz.toInt()} kHz Lossless Surround"
                    else
                        "$bitrateKbps kbps Surround",
                    isLossless = isLosslessDTS,
                    isHiRes = isLosslessDTS,
                    isDolby = false,
                    isDTS = true,
                    bitDepthEstimate = if (isLosslessDTS) estimatedBitDepth else 16,
                    category = if (isLosslessDTS) "Lossless Surround" else "Lossy Surround"
                )
            }

            // Dolby Surround (5.1/7.1 FLAC, ALAC, etc.)
            finalIsLossless && channelCount > 2 -> {
                val channelLabel = when (channelCount) {
                    6 -> "5.1"
                    8 -> "7.1"
                    else -> "${channelCount}ch"
                }
                AudioQuality(
                    qualityType = QualityType.LOSSLESS_SURROUND,
                    qualityLabel = "Dolby Surround $channelLabel",
                    qualityDescription = "${estimatedBitDepth}-bit / ${sampleRateKhz.toInt()} kHz Surround",
                    isLossless = true,
                    isHiRes = estimatedBitDepth >= 24 || sampleRateHz > 48000,
                    isDolby = true,
                    isDTS = false,
                    bitDepthEstimate = estimatedBitDepth,
                    category = "Dolby Surround"
                )
            }

            // Hi-Res Studio Master (192 kHz, 24-bit lossless, >4600 kbps)
            finalIsLossless && sampleRateHz >= 192000 && estimatedBitDepth >= 24 -> {
                AudioQuality(
                    qualityType = QualityType.HI_RES_STUDIO_MASTER,
                    qualityLabel = "Studio Master",
                    qualityDescription = "${estimatedBitDepth}-bit / ${sampleRateKhz.toInt()} kHz Lossless",
                    isLossless = true,
                    isHiRes = true,
                    isDolby = false,
                    isDTS = false,
                    bitDepthEstimate = estimatedBitDepth,
                    category = "Lossless"
                )
            }

            // Hi-Res Lossless (48-96 kHz, typically 24-bit, >2000 kbps)
            finalIsLossless && (sampleRateHz > 48000 || estimatedBitDepth >= 24) -> {
                Log.d(TAG, "Detected: HI_RES_LOSSLESS (sampleRate=${sampleRateHz}Hz, bitDepth=$estimatedBitDepth-bit)")
                AudioQuality(
                    qualityType = QualityType.HI_RES_LOSSLESS,
                    qualityLabel = "Hi-Res Lossless",
                    qualityDescription = "${estimatedBitDepth}-bit / ${sampleRateKhz.toInt()} kHz Lossless",
                    isLossless = true,
                    isHiRes = true,
                    isDolby = false,
                    isDTS = false,
                    bitDepthEstimate = estimatedBitDepth,
                    category = "Lossless"
                )
            }

            // CD Quality Lossless (44.1 kHz, 16-bit, ~1411 kbps)
            finalIsLossless -> { // Catches any remaining lossless formats
                Log.d(TAG, "Detected: CD_QUALITY_LOSSLESS (sampleRate=${sampleRateHz}Hz, bitDepth=$estimatedBitDepth-bit)")
                AudioQuality(
                    qualityType = QualityType.CD_QUALITY_LOSSLESS,
                    qualityLabel = "Lossless",
                    qualityDescription = "${estimatedBitDepth}-bit / ${sampleRateKhz.toInt()} kHz Lossless",
                    isLossless = true,
                    isHiRes = false,
                    isDolby = false,
                    isDTS = false,
                    bitDepthEstimate = estimatedBitDepth,
                    category = "Lossless"
                )
            }

            // Lossy Compressed (MP3, AAC, etc.)
            else -> {
                AudioQuality(
                    qualityType = QualityType.LOSSY_COMPRESSED,
                    qualityLabel = "Lossy",
                    qualityDescription = "$bitrateKbps kbps / ${sampleRateKhz.toInt()} kHz",
                    isLossless = false,
                    isHiRes = false,
                    isDolby = false,
                    isDTS = false,
                    bitDepthEstimate = 16, // Lossy formats don't have a meaningful bit depth after encoding
                    category = "Lossy"
                )
            }
        }
    }

    /**
     * Get a simple quality badge label for UI display
     */
    fun getQualityBadge(quality: AudioQuality): String {
        return when (quality.qualityType) {
            QualityType.HI_RES_STUDIO_MASTER -> "Studio Master"
            QualityType.HI_RES_LOSSLESS -> "Hi-Res"
            QualityType.CD_QUALITY_LOSSLESS -> "Lossless"
            QualityType.LOSSLESS_SURROUND -> quality.qualityLabel // "Dolby Surround 5.1" etc.
            QualityType.DOLBY_LOSSLESS -> when {
                quality.qualityLabel.contains("Atmos") -> "Dolby Atmos"
                else -> "Dolby TrueHD"
            }
            QualityType.DOLBY_LOSSY_SURROUND -> when {
                quality.qualityLabel.contains("Plus") -> "Dolby Digital Plus"
                else -> "Dolby Digital"
            }
            QualityType.DTS_SURROUND -> when {
                quality.isLossless -> "DTS-HD Master Audio"
                else -> "DTS"
            }
            QualityType.LOSSY_COMPRESSED -> when {
                quality.qualityDescription.contains("320") -> "320kbps"
                quality.qualityDescription.contains("256") -> "256kbps"
                quality.qualityDescription.contains("192") -> "192kbps"
                quality.qualityDescription.contains("128") -> "128kbps"
                else -> "Lossy"
            }
            QualityType.UNKNOWN -> ""
        }
    }

    /**
     * Determine if audio qualifies as "Hi-Res" (≥48 kHz or lossless with >2000 kbps)
     */
    fun isHiResAudio(sampleRateHz: Int, bitrateKbps: Int, isLossless: Boolean): Boolean {
        return sampleRateHz >= 48000 || (isLossless && bitrateKbps >= 2000)
    }

    /**
     * Get color indicator for quality level (for UI theming)
     * Returns: "excellent", "good", "standard", "unknown"
     */
    fun getQualityColorIndicator(quality: AudioQuality): String {
        return when (quality.qualityType) {
            QualityType.HI_RES_STUDIO_MASTER, 
            QualityType.DOLBY_LOSSLESS,
            QualityType.LOSSLESS_SURROUND -> "excellent"
            
            QualityType.HI_RES_LOSSLESS, 
            QualityType.CD_QUALITY_LOSSLESS -> "good"
            
            QualityType.LOSSY_COMPRESSED,
            QualityType.DOLBY_LOSSY_SURROUND,
            QualityType.DTS_SURROUND -> "standard"
            
            QualityType.UNKNOWN -> "unknown"
        }
    }
}
