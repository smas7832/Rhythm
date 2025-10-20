package chromahub.rhythm.app.util

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

    /**
     * Represents the audio quality tier
     */
    enum class QualityType {
        LOSSY_COMPRESSED,      // MP3, AAC, OGG (128-320 kbps)
        CD_QUALITY_LOSSLESS,   // FLAC, ALAC 16/44.1
        HI_RES_LOSSLESS,       // FLAC, ALAC 24/48+ kHz
        HI_RES_STUDIO_MASTER,  // FLAC, ALAC 24/192 kHz
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
            "TRUEHD", "MLP", "DTS-HD", "DTS-HD MA"
        )

        // Detect Dolby variants
        val isDolbyCodec = normalizedCodec in listOf(
            "AC-3", "AC3", "E-AC-3", "EAC3", "DOLBY DIGITAL", "DOLBY DIGITAL PLUS",
            "TRUEHD", "DOLBY TRUEHD", "ATMOS", "DOLBY ATMOS", "MLP"
        )

        // Detect DTS variants
        val isDTSCodec = normalizedCodec.contains("DTS")

        // Estimate bit depth if not provided
        val estimatedBitDepth = when {
            bitDepth > 0 -> bitDepth
            // Use bitrate-based calculation for lossless with improved thresholds
            isLosslessCodec && bitrateKbps > 0 && sampleRateHz > 0 && channelCount > 0 -> {
                val calculated = (bitrateKbps * 1000) / (sampleRateHz * channelCount)
                // Reference: CD (44.1kHz/16-bit) = 1,411 kbps → 16 bits/sample
                //           Hi-Res (96kHz/24-bit) = 4,608 kbps → 24 bits/sample
                when {
                    calculated >= 30 -> 32  // 32-bit (30+ bits/sample)
                    calculated >= 22 -> 24  // 24-bit (22-29 bits/sample)
                    calculated >= 14 -> 16  // 16-bit (14-21 bits/sample)
                    else -> 16
                }
            }
            // Fallback to sample rate heuristics
            sampleRateHz >= 96000 -> 24   // Hi-Res is typically 24-bit
            sampleRateHz >= 48000 -> 24   // Hi-Res audio
            isLosslessCodec -> 16         // CD quality default
            else -> 16                    // Default assumption
        }

        // Determine quality type and details
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

            // Hi-Res Studio Master (192 kHz, 24-bit lossless, >4600 kbps)
            isLosslessCodec && sampleRateHz >= 192000 && estimatedBitDepth >= 24 -> {
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
            isLosslessCodec && sampleRateHz >= 48000 && sampleRateHz < 192000 -> {
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
            isLosslessCodec && sampleRateHz <= 48000 -> {
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

            // Lossy compressed (MP3, AAC, OGG, etc.)
            !isLosslessCodec -> {
                val qualityDescription = when {
                    bitrateKbps >= 320 -> "320 kbps High Quality"
                    bitrateKbps >= 256 -> "256 kbps"
                    bitrateKbps >= 192 -> "192 kbps"
                    bitrateKbps >= 128 -> "128 kbps"
                    else -> "$bitrateKbps kbps"
                }
                
                AudioQuality(
                    qualityType = QualityType.LOSSY_COMPRESSED,
                    qualityLabel = "Lossy / Compressed",
                    qualityDescription = qualityDescription,
                    isLossless = false,
                    isHiRes = false,
                    isDolby = false,
                    isDTS = false,
                    bitDepthEstimate = 16,
                    category = "Lossy"
                )
            }

            // Unknown/fallback
            else -> {
                AudioQuality(
                    qualityType = QualityType.UNKNOWN,
                    qualityLabel = "Unknown",
                    qualityDescription = "Unable to determine quality",
                    isLossless = false,
                    isHiRes = false,
                    isDolby = isDolbyCodec,
                    isDTS = isDTSCodec,
                    bitDepthEstimate = 0,
                    category = "Unknown"
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
            QualityType.DOLBY_LOSSLESS -> "excellent"
            
            QualityType.HI_RES_LOSSLESS, 
            QualityType.CD_QUALITY_LOSSLESS -> "good"
            
            QualityType.LOSSY_COMPRESSED,
            QualityType.DOLBY_LOSSY_SURROUND,
            QualityType.DTS_SURROUND -> "standard"
            
            QualityType.UNKNOWN -> "unknown"
        }
    }
}
