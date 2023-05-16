# Proguard Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Kotlin Serialization
-keepclassmembers,allowoptimization class com.sendbird.uikit.** {
    *** Companion;
}
-keepclassmembers,allowoptimization class com.sendbird.uikit.** {
    kotlinx.serialization.KSerializer serializer(...);
}

