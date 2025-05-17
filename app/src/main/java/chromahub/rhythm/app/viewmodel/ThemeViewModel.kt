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
    
    // Function to update system theme usage
    fun setUseSystemTheme(useSystem: Boolean) {
        _useSystemTheme.value = useSystem
    }
    
    // Function to update dark mode
    fun setDarkMode(isDark: Boolean) {
        _darkMode.value = isDark
    }
} 