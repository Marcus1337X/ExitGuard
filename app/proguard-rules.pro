# ExitGuard Proguard & R8 Optimization Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Kotlinx Serialization
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class com.exitguard.app.model.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# AndroidX Keep
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

