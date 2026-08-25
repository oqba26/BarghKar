# ============================================================
# Bargh Kar - ProGuard/R8 Rules
# ============================================================

# ---------------------
# Kotlinx Serialization
# ---------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all @Serializable data classes
-keep,includedescriptorclasses class com.oqba26.barghkar.**$$serializer { *; }
-keepclassmembers class com.oqba26.barghkar.** {
    *** Companion;
}
-keepclasseswithmembers class com.oqba26.barghkar.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------
# Room Database
# ---------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class com.oqba26.barghkar.data.local.entity.** { *; }
-keep class com.oqba26.barghkar.data.local.dao.** { *; }
-keep class com.oqba26.barghkar.data.local.AppDatabase { *; }

# ---------------------
# Ktor
# ---------------------
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ---------------------
# Supabase
# ---------------------
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ---------------------
# OkHttp
# ---------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ---------------------
# AndroidX / Compose
# ---------------------
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.lifecycle.AndroidViewModel

# ---------------------
# General
# ---------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove all logs in release build
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}
