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
import { enableScreens } from 'react-native-screens';
import SplashScreen from 'react-native-splash-screen';
import Logger from './src/common/Logger';
enableScreens();

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
