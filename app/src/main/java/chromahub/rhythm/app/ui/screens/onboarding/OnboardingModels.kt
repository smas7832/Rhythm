package chromahub.rhythm.app.ui.screens.onboarding

enum class OnboardingStep {
    WELCOME,
    PERMISSIONS,
    AUDIO_PLAYBACK, // New step for audio and playback settings
    THEMING,
    LIBRARY_SETUP, // New step for library organization preferences
    UPDATER,
    COMPLETE
}

sealed class PermissionScreenState {
    object Loading : PermissionScreenState()
    object PermissionsRequired : PermissionScreenState()
    object ShowRationale : PermissionScreenState()
    object RedirectToSettings : PermissionScreenState()
    object PermissionsGranted : PermissionScreenState()
}
