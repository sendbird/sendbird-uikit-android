# Uncomment for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

# Kotlin Serialization
-keepclassmembers,allowoptimization class com.sendbird.uikit.** {
    *** Companion;
}
-keepclassmembers,allowoptimization class com.sendbird.uikit.** {
    kotlinx.serialization.KSerializer serializer(...);
}

