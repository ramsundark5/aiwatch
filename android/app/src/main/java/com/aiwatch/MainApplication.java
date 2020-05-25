package com.aiwatch;

import android.app.Application;
import android.content.Context;

import com.bugsnag.android.Bugsnag;
import com.facebook.react.ReactApplication;
import com.brentvatne.react.ReactVideoPackage;
import com.reactnativecommunity.rnpermissions.RNPermissionsPackage;
import com.reactnativecommunity.webview.RNCWebViewPackage;
import com.rnappauth.RNAppAuthPackage;
import com.reactnativecommunity.netinfo.NetInfoPackage;
import com.sbugert.rnadmob.RNAdMobPackage;
import com.dooboolab.RNIap.RNIapPackage;
import org.devio.rn.splashscreen.SplashScreenReactPackage;
import com.lugg.ReactNativeConfig.ReactNativeConfigPackage;
import com.bugsnag.BugsnagReactNative;

import co.apptailor.googlesignin.RNGoogleSigninPackage;
import com.horcrux.svg.SvgPackage;
import com.swmansion.rnscreens.RNScreensPackage;
import com.aiwatch.media.db.ObjectBox;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {


  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            new ReactVideoPackage(),
            new RNAppAuthPackage(),
            new SvgPackage(),
            new NetInfoPackage(),
            new RNAdMobPackage(),
            new RNIapPackage(),
            new SplashScreenReactPackage(),
            new ReactNativeConfigPackage(),
            BugsnagReactNative.getPackage(),
            new RNGoogleSigninPackage(),
            new RNScreensPackage(),
            new RNGestureHandlerPackage(),
            new VectorIconsPackage(),
            new RNSmartCamPackage(),
            new RNCWebViewPackage(),
            new RNPermissionsPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }

    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ObjectBox.init(this);
    SoLoader.init(this, /* native exopackage */ false);
    initializeFlipper(this); // Remove this line if you don't want Flipper enabled
  }

  /**
   * Loads Flipper in React Native templates.
   *
   * @param context
   */
  private static void initializeFlipper(Context context) {
    if (BuildConfig.DEBUG) {
      try {
        /*
         We use reflection here to pick up the class that initializes Flipper,
        since Flipper library is not available in release mode
        */
        Class<?> aClass = Class.forName("com.facebook.flipper.ReactNativeFlipper");
        aClass.getMethod("initializeFlipper", Context.class).invoke(null, context);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }
}
