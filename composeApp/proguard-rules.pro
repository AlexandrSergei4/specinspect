# Compose dialog windows call this AndroidX compat setter internally. Android 15+
# ignores/deprecates the underlying platform API, and Play Console flags the
# method reference in release dex, so strip the unused setter call from dialogs.
-assumenosideeffects class androidx.core.view.WindowCompat {
    public static void setDecorFitsSystemWindows(android.view.Window, boolean);
}

-dontwarn androidx.test.platform.app.InstrumentationRegistry
