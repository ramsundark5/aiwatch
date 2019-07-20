# Do not optimize/shrink LibVLC, because of native code
-keep class org.videolan.** { *; }
-keep class nl.bravobit.** { *; }

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }