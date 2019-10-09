# Do not optimize/shrink LibVLC, because of native code
-keep class org.videolan.** { *; }
-keep class co.apptailor.** { *; }
-keep class nl.bravobit.** { *; }
-keep class org.mp4parser.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep class com.google.api.** {*;}
-keep class com.aiwatch.models.** {*;}
-keep class org.apache.** { *; }
-keep class io.evercam.** {*;}
-keep class com.facebook.hermes.unicode.** { *; }

#mail jars
-keep class javax.** {*;}
-keep class com.sun.** {*;}
-keep class myjava.** {*;}
-keep class org.apache.harmony.** {*;}
-keep public class Mail {*;}
-dontwarn java.awt.**
-dontwarn java.beans.Beans
-dontwarn javax.security.**

# Don't show warnings for the following libraries
-dontwarn android.support.v7.**

