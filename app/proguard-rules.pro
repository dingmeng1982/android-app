-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

-dontobfuscate

# prevent multi dex caused NoSuchProviderException
-keep class org.whispersystems.** { *; }

-keep class one.mixin.android.** { *; }

# webrtc
-dontwarn org.webrtc.NetworkMonitorAutoDetect
-dontwarn android.net.Network
-keep class org.webrtc.** { *; }