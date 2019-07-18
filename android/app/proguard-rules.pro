# Do not optimize/shrink LibVLC, because of native code
-keep class org.videolan.** { *; }
-keep class nl.bravobit.** { *; }