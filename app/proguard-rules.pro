# ============================================================
# Bargh Kar - ProGuard/R8 Rules
# ============================================================

# ---------------------
# SQLCipher rules
# ---------------------
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-keepclassmembers class net.sqlcipher.database.SQLiteDatabase {
    private long mNativeHandle;
}
-dontwarn net.sqlcipher.**

# ---------------------
# Kotlinx Serialization
# ---------------------
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
# Tink / AndroidX Security rules
# ---------------------
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ---------------------
# General
# ---------------------
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Remove all logs in release build
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}
