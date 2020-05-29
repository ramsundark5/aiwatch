import React from 'react';
import { CameraTabRoute } from './src/cameras/CameraTabRoute';
import { EventTabRoute } from './src/events/EventTabRoute';
import { SettingsTabRoute } from './src/settings/SettingsTabRoute';
import { HelpTabRoute } from './src/help/HelpTabRoute';
import { createAppContainer } from 'react-navigation';
import { createMaterialBottomTabNavigator } from 'react-navigation-material-bottom-tabs';
import Theme from './src/common/Theme';
import { Provider } from 'react-redux';
import { store } from './src/store/Store';
import { useScreens } from 'react-native-screens';
import SplashScreen from 'react-native-splash-screen';
import Logger from './src/common/Logger';
//useScreens();

const AppContainer = createAppContainer(
  createMaterialBottomTabNavigator(
    {
      CameraTab: CameraTabRoute,
      EventsTab: EventTabRoute,
      SettingsTab: SettingsTabRoute,
      HelpTab: HelpTabRoute
    },
    {
      barStyle: {backgroundColor: 'white'},
      activeColor: Theme.primary,
      activeTintColor: Theme.primary,
      shifting: false
    }
  )
);

//prefix is used for deeplinking
const prefix = 'https://aiwatch.live/';

export default class App extends React.Component{

  componentDidMount() {
    SplashScreen.hide();
    //init();
  }

  render(){
    return(
      <Provider store={store}>
        <AppContainer uriPrefix={prefix}/>
      </Provider>
    )
  }
}

function init() {
    console.log = interceptLog(console.log);
    console.info = interceptLog(console.info);
    console.error = interceptLog(console.error);
    //console.debug = interceptLog(console.debug);
}

function interceptLog(originalFn) {
  return function() {
      try{
        const args = Array.prototype.slice.apply(arguments);
        let result = '';
        for (let i = 0; i < args.length; i++) {
            const arg = args[i];
            if (!arg || (typeof arg === 'string') || (typeof arg === 'number')) {
                result += arg;
            }
            else {
                result += JSON.stringify(arg);
            }
        }
        //originalFn.call(console, 'INTERCEPTED LOG: ' + result);
        Logger.log(result);
      }catch(err){
        //swallow the exception
      }
      return originalFn.apply(console, arguments);
  };
}
