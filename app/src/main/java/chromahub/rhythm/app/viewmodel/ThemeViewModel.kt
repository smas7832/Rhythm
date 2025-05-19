package chromahub.rhythm.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    // Theme state
    private val _useSystemTheme = MutableStateFlow(false)
    val useSystemTheme: StateFlow<Boolean> = _useSystemTheme.asStateFlow()
    
    private val _darkMode = MutableStateFlow(true)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()
    
    // Dynamic color state (for Monet theme)
    private val _useDynamicColors = MutableStateFlow(false)
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()
    
    // Function to update system theme usage
    fun setUseSystemTheme(useSystem: Boolean) {
        _useSystemTheme.value = useSystem
        // When system theme is enabled, also enable dynamic colors (Monet)
        _useDynamicColors.value = useSystem
    }
    
    // Function to update dark mode
    fun setDarkMode(isDark: Boolean) {
        _darkMode.value = isDark
    }
} 