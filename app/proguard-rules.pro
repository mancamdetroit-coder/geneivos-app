َ# Add project specific ProGuard rules here.
-keep class org.geneivos.app.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
