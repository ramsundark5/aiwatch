import React, { Component } from 'react';
import CameraView from './src/cameras/CameraView';
import EventsView from './src/events/EventsView';
import EditCamera from './src/cameras/EditCamera';
import { createStackNavigator, createAppContainer } from 'react-navigation';
import { createMaterialBottomTabNavigator } from 'react-navigation-material-bottom-tabs';
import Ionicons from 'react-native-vector-icons/Ionicons';
import EventVideo from './src/events/EventVideo';
import Settings from './src/settings/Settings'
import Theme from './src/common/Theme';
import { Provider } from 'react-redux';
import { store } from './src/store/Store';
import { useScreens } from 'react-native-screens';
useScreens();

const CameraStack = createStackNavigator({
  CameraView: {
    screen: CameraView,
    navigationOptions: () => ({
      header: null
    }),
  },
  EditCamera: EditCamera,
});

const EventStack = createStackNavigator({
  Events: EventsView,
  EventVideo: {
    screen: EventVideo,
    navigationOptions: {
      header: null
    }
  }
}, {
  mode: 'modal',
  headerMode: 'none',
});

EventStack.navigationOptions = ({ navigation }) => {

  let tabBarVisible = true;
   let headerMode = 'none';
  let routeName = navigation.state.routes[navigation.state.index].routeName

  if ( routeName == 'EventVideo' ) {
      tabBarVisible = false,
      header = null
  }

  return {
      headerMode,
      tabBarVisible,
  }
}

const SettingsStack = createStackNavigator({
  Settings: {
    screen: Settings,
    path: 'oauth2redirect',
  }
});

const tabBarIcon = name => ({ tintColor, horizontal }) => (
  <Ionicons name={name} color={tintColor} size={horizontal ? 17 : 24} />
);

const prefix = 'https://aiwatch.live/';

const AppContainer = createAppContainer(
  createMaterialBottomTabNavigator(
    {
      CameraTab: {
        screen: CameraStack,
        navigationOptions: {
          tabBarLabel: 'Camera',
          tabBarIcon: tabBarIcon('ios-videocam')
        }
      },
      EventsTab: {
        screen: EventStack,
        navigationOptions: {
          tabBarLabel: 'Events',
          tabBarIcon: tabBarIcon('ios-calendar')
        }
      },
      SettingsTab: {
        screen: SettingsStack,
        navigationOptions: {
          tabBarLabel: 'Settings',
          tabBarIcon: tabBarIcon('ios-settings')
        }
      }
    },
    {
      //initialRouteName: 'SettingsTab',
      barStyle: {backgroundColor: 'white'},
      activeColor: Theme.primary,
      activeTintColor: Theme.primary
    }
  )
);

const AppContainerPortal = () => {
  return(
    <Provider store={store}>
        <AppContainer uriPrefix={prefix}/>
    </Provider>
  )
}
export default AppContainerPortal;
