# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\nsnik\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-optimizationpasses 5
-allowaccessmodification
-assumenosideeffects
-verbose

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

-libraryjars <java.home>/lib/rt.jar

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

-dontwarn okio.**

