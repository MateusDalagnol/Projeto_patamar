# OSMDroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# MapLibre (se usar Opção B)
-keep class org.maplibre.** { *; }
-keep class com.mapbox.** { *; }
-dontwarn org.maplibre.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keepclasseswithmembernames class * { @javax.inject.Inject <init>(...); }

# Data classes (não obfuscar nomes dos campos do Room)
-keep class com.patamar.app.data.model.** { *; }

# Gson
-keepattributes Signature
-keepattributes Annotation
-keep class com.google.gson.** { *; }
