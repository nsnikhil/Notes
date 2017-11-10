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

-assumenosideeffects class timber.log.Timber {
 public static *** d(...);
 public static *** i(...);
 public static *** v(...);
}

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-dontwarn com.google.errorprone.annotations.*

-dontwarn com.f2prateek.dart.internal.**
-keep class **$$ExtraInjector { *; }
-keepclasseswithmembernames class * {
    @com.f2prateek.dart.* <fields>;
}
-keep class **Henson { *; }
-keep class **$$IntentBuilder { *; }

-keep class * implements android.arch.lifecycle.GeneratedAdapter {<init>(...);}

-keepclasseswithmembers class * implements android.arch.lifecycle.GenericLifecycleObserver {
<init>(...);
}
-keepclassmembers class android.arch.lifecycle.Lifecycle$* { *; }
-keepclassmembers class * {
    @android.arch.lifecycle.OnLifecycleEvent *;
}
-keepclassmembers class * extends android.arch.lifecycle.ViewModel {
<init>(...);
}

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.GeneratedAppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-dontwarn sun.misc.Unsafe

-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

-dontwarn okio.**

-dontwarn javax.annotation.**

-dontwarn org.mockito.**