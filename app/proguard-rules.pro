# Project-specific ProGuard rules for DNotifyManager

# Keep Room Database and Entities
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase
-keep class com.app.dnotifymanager.data.** { *; }

# Keep Notification Listener Service
-keep class com.app.dnotifymanager.service.NotificationReceiver { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    java.lang.String name;
}

# General optimizations
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Strip Log calls in release to save size and improve security
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep Compose internal names for stability
-keepclassmembers class androidx.compose.runtime.Recomposer {
    private androidx.compose.runtime.Recomposer$Companion Companion;
}
