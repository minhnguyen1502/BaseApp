# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Preserve Gson classes
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes EnclosingMethod
# Preserve OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-dontwarn javax.annotation.**

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class * extends com.google.gson.TypeAdapter
-keep class kotlinx.parcelize.** { *; }
-keep class * implements android.os.Parcelable { *; }
-keep class com.mbridge.** { *; }
-keep class com.google.android.gms.base.** { *; }


# Application classes that will be serialized/deserialized over Gson
-keep class com.wavez.base.ui.worldcup.** { *; }
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod


-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keep class * implements android.os.Parcelable { *; }

# Keep Google AdvertisingIdClient classes
-keep class com.google.android.gms.ads.identifier.** { *; }


#ads
-keep class com.google.android.gms.common.ConnectionResult {
   int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
   com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
   java.lang.String getId();
   boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
-keep public class com.adjust.sdk.** { *; }
-keep public class com.android.installreferrer.** { *; }

-keep class com.google.android.gms.ads.identifier.** { *; }
-keep public interface com.applovin.mediation** {*; }

-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.common.internal.** { *; }
-keep class com.google.android.gms.ads.** { *; }

-keep class com.applovin.mediation.adapters.** { *; }
-keep class com.google.ads.mediation.** { *; }
-keep class com.google.android.gms.common.providers.** { *; }
-keep class com.google.android.gms.common.util.** { *; }
-keep class android.webkit.** { *; }
-keep class com.google.android.play.core.** { *; }
# Google Ads
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.ads.**

-keep class com.google.android.gms.internal.** { *; }
-keep class bb** { *; }
-keep class bakp** { *; }
-keep class bbef** { *; }
-keep class bbge** { *; }
-dontwarn com.google.android.gms.internal.**
-dontwarn com.google.android.gms.ads.**

# AppLovin MAX Mediation
-keep class com.applovin.** { *; }
-keep class com.google.ads.mediation.applovin.** { *; }
-dontwarn com.applovin.**

# WebView cho quảng cáo
-keep class com.android.webview.chromium.** { *; }
-keep class org.chromium.** { *; }
-keepclassmembers class * extends android.webkit.WebView {
   public *;
}
-keep class android.webkit.** { *; }
-dontwarn android.webkit.**

##facebook
#-keep class com.facebook.ads.** { *; }
#-dontwarn com.facebook.ads.**


# Pangle (Bytedance) SDK — required for mediation adapter to load runtime
# classes. Thiếu rules này, R8 obfuscate hết → adapter trả "no fill" silently
# và AdMob bỏ qua Pangle network.
-keep class com.bytedance.** { *; }
-keep class sg.bigo.ads.** { *; }
-keep class com.bytedance.sdk.openadsdk.R$* { *; }
-keepclassmembers class com.bytedance.sdk.openadsdk.R$* { *; }
-keep class com.bytedance.sdk.** { *; }
-keep interface com.bytedance.sdk.** { *; }
-keep class com.bytedance.embedapplog.** { *; }
-keep class com.bytedance.android.** { *; }
-keep class com.bykv.** { *; }
-keep class com.byted.** { *; }
-keep class com.pangle.** { *; }
-keep class com.tt.** { *; }
-dontwarn com.bytedance.**
-dontwarn com.bykv.**
-dontwarn com.byted.**
-dontwarn com.pangle.**

# Media3 / ExoPlayer — DefaultMediaSourceFactory loads DASH, HLS, RTSP factories
# via Class.forName() at runtime, so R8 must not strip them.
-keep class androidx.media3.exoplayer.dash.** { *; }
-keep class androidx.media3.exoplayer.hls.** { *; }
-keep class androidx.media3.exoplayer.rtsp.** { *; }
-keep class androidx.media3.exoplayer.smoothstreaming.** { *; }
