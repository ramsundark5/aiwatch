# Do not optimize/shrink LibVLC, because of native code
-keep class org.videolan.** { *; }
-keep class co.apptailor.** { *; }
-keep class nl.bravobit.** { *; }
-keep class org.mp4parser.** { *; }
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep class com.google.api.** {*;}
