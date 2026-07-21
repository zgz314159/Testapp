# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep StAX/Aalto implementations that POI locates via reflection
-keep class com.fasterxml.aalto.** { *; }
-keep class org.codehaus.stax2.** { *; }
-keep class javax.xml.stream.** { *; }
-dontwarn com.fasterxml.aalto.**
-dontwarn org.codehaus.stax2.**
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

# -------------------- Shrinking rules for Apache POI on Android --------------------
# POI optionally references desktop AWT/Swing/Batik/Saxon/etc. These are not present on Android.
# We don't use those code paths at runtime, so suppress missing-class warnings to allow R8 to shrink them away.
-dontwarn java.awt.**
-dontwarn java.awt.geom.**
-dontwarn javax.xml.stream.**
-dontwarn org.apache.batik.**
-dontwarn net.sf.saxon.**
-dontwarn org.osgi.framework.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.slf4j.**

# Log4j API creates its message factories reflectively. Apache POI initializes
# DataFormatter through StatusLogger, so removing these constructors crashes
# only in minified builds (ExceptionInInitializerError during Excel import).
-keep class org.apache.logging.log4j.message.** { *; }

# XMLBeans/OpenXML are used by POI; keep core types and suppress warnings for optional parts
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class org.apache.poi.schemas.** { *; }
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.**

# Keep Apache POI core packages and Apache Commons dependencies used by POI.
# XSLF(PowerPoint) 与 sl.draw 渲染链引用 Android 缺失的 java.awt/Batik，项目从不使用；
# 将其排除出 keep，让 R8 剔除，消除 "SVGUserAgent does not type check" 告警并减小 APK。
-keep class !org.apache.poi.xslf.**,!org.apache.poi.sl.draw.**,org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-keep class org.apache.commons.** { *; }
-dontwarn org.apache.commons.**

# (Optional) If you see warnings about GenericRecordJsonWriter or imaging code, it's fine to ignore; it's desktop-only.
# -----------------------------------------------------------------------------------

# ------ Strip common logging in release (remove method bodies) ------
-assumenosideeffects class android.util.Log {
	public static *** d(...);
	public static *** v(...);
	public static *** i(...);
	public static *** w(...);
	public static *** e(...);
}

# Remove Kotlin/Java assertions in release
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void check*(...);
}
-assumenosideeffects class java.lang.AssertionError {
	<init>(...);
}
