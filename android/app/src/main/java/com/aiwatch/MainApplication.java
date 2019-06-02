package com.aiwatch;

import android.app.Application;

import com.facebook.react.ReactApplication;
import com.bugsnag.BugsnagReactNative;
import co.apptailor.googlesignin.RNGoogleSigninPackage;
import com.swmansion.rnscreens.RNScreensPackage;
import com.aiwatch.media.db.ObjectBox;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.yuanzhou.vlc.ReactVlcPlayerPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.github.yamill.orientation.OrientationPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
            BugsnagReactNative.getPackage(),
            new RNGoogleSigninPackage(),
            new RNScreensPackage(),
            new RNGestureHandlerPackage(),
            new VectorIconsPackage(),
            new OrientationPackage(),
            new ReactVlcPlayerPackage(),
            new RNSmartCamPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
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
  }
}
