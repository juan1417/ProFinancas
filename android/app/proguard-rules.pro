# ProGuard rules for ProFinanzas
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Gson
-keep class com.profinanzas.core.data.model.** { *; }
-keep class com.profinanzas.features.wallet.presentation.viewmodel.StoredCard { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ML Kit
-keep class com.google.mlkit.** { *; }
