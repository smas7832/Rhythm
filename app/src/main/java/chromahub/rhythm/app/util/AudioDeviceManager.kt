package chromahub.rhythm.app.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import chromahub.rhythm.app.data.PlaybackLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utility class to manage audio output devices
 */
class AudioDeviceManager(private val context: Context) {
    private val TAG = "AudioDeviceManager"
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val bluetoothManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    } else {
        null
    }
    
    private val bluetoothAdapter = bluetoothManager?.adapter
    
    // Device lists
    private val _availableDevices = MutableStateFlow<List<PlaybackLocation>>(emptyList())
    val availableDevices: StateFlow<List<PlaybackLocation>> = _availableDevices.asStateFlow()
    
    private val _currentDevice = MutableStateFlow<PlaybackLocation?>(null)
    val currentDevice: StateFlow<PlaybackLocation?> = _currentDevice.asStateFlow()
    
    // Default device IDs
    companion object {
        const val DEVICE_SPEAKER = "speaker"
        const val DEVICE_WIRED_HEADSET = "wired_headset"
        const val DEVICE_BLUETOOTH_PREFIX = "bt_"
    }
    
    init {
        refreshDevices()
    }
    
    /**
     * Refresh the list of available audio devices
     */
    fun refreshDevices() {
        Log.d(TAG, "Refreshing audio devices")
        val devices = mutableListOf<PlaybackLocation>()
        
        // Always add speaker
        devices.add(
            PlaybackLocation(
                id = DEVICE_SPEAKER,
                name = "Phone Speaker",
                icon = 0 // Use appropriate icon
            )
        )
        
        // Check for wired headset
        if (audioManager.isWiredHeadsetOn) {
            devices.add(
                PlaybackLocation(
                    id = DEVICE_WIRED_HEADSET,
                    name = "Wired Headphones",
                    icon = 0 // Use appropriate icon
                )
            )
        }
        
        // Add Bluetooth devices
        getConnectedBluetoothDevices().forEach { (name, address) ->
            devices.add(
                PlaybackLocation(
                    id = "${DEVICE_BLUETOOTH_PREFIX}$address",
                    name = name,
                    icon = 0 // Use appropriate icon
                )
            )
        }
        
        // Update the available devices
        _availableDevices.value = devices
        
        // Set current device if not already set
        if (_currentDevice.value == null) {
            // Prefer Bluetooth > Wired > Speaker
            val bluetoothDevice = devices.find { it.id.startsWith(DEVICE_BLUETOOTH_PREFIX) }
            val wiredDevice = devices.find { it.id == DEVICE_WIRED_HEADSET }
            
            _currentDevice.value = bluetoothDevice ?: wiredDevice ?: devices.firstOrNull()
        } else {
            // Check if current device is still available
            val deviceStillAvailable = devices.any { it.id == _currentDevice.value?.id }
            if (!deviceStillAvailable) {
                // Select a new device
                _currentDevice.value = devices.firstOrNull()
            }
        }
        
        Log.d(TAG, "Available devices: ${devices.joinToString { it.name }}")
        Log.d(TAG, "Current device: ${_currentDevice.value?.name ?: "None"}")
    }
    
    /**
     * Set the current audio output device
     */
    fun setCurrentDevice(device: PlaybackLocation) {
        Log.d(TAG, "Setting current device to: ${device.name}")
        _currentDevice.value = device
        
        // In a real implementation, you would route audio to the selected device
        // This might involve using AudioManager or MediaRouter APIs
        // For Bluetooth devices, you might need to use the Bluetooth APIs
        
        // For this example, we'll just log the change
        val deviceType = when {
            device.id == DEVICE_SPEAKER -> "Speaker"
            device.id == DEVICE_WIRED_HEADSET -> "Wired Headset"
            device.id.startsWith(DEVICE_BLUETOOTH_PREFIX) -> "Bluetooth"
            else -> "Unknown"
        }
        
        Log.d(TAG, "Audio output switched to $deviceType: ${device.name}")
    }
    
    /**
     * Get a list of connected Bluetooth audio devices
     * @return List of device name and address pairs
     */
    private fun getConnectedBluetoothDevices(): List<Pair<String, String>> {
        val devices = mutableListOf<Pair<String, String>>()
        
        try {
            // Check if Bluetooth is enabled
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                Log.d(TAG, "Bluetooth is not enabled")
                return emptyList()
            }
            
            // Get connected A2DP (audio) devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android 8.0+
                val bluetoothDevices = bluetoothAdapter.bondedDevices
                for (device in bluetoothDevices) {
                    // Check if it's an audio device (A2DP)
                    if (device.type == BluetoothProfile.A2DP) {
                        devices.add(Pair(device.name ?: "Unknown Device", device.address))
                    }
                }
            } else {
                // For older Android versions
                // This is a simplified approach that might not be accurate
                // In a real app, you would use BluetoothProfile.ServiceListener
                val bluetoothDevices = bluetoothAdapter.bondedDevices
                for (device in bluetoothDevices) {
                    // We can't easily determine if it's an audio device in older Android versions
                    // So we'll include all paired devices (not ideal)
                    devices.add(Pair(device.name ?: "Unknown Device", device.address))
                }
            }
            
            // Alternative approach using AudioManager (Android 6.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (device in audioDevices) {
                    if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                        // For Bluetooth devices from AudioManager, we don't have the address
                        // So we'll use a unique identifier based on the product name
                        val deviceName = device.productName.toString()
                        val deviceId = deviceName.replace(" ", "_").lowercase()
                        
                        // Only add if not already added
                        if (devices.none { it.first == deviceName }) {
                            devices.add(Pair(deviceName, deviceId))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Bluetooth devices", e)
        }
        
        return devices
    }
    
    /**
     * Check if audio is currently playing through a Bluetooth device
     */
    fun isBluetoothActive(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            devices.any { 
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP && 
                it.isSink
            }
        } else {
            // For older versions, check if the current device is Bluetooth
            _currentDevice.value?.id?.startsWith(DEVICE_BLUETOOTH_PREFIX) == true
        }
    }
} 