import React from 'react';
import { createStackNavigator } from 'react-navigation-stack';
import { tabBarIcon } from '../common/TabBarIcon';
import EventsView from './EventsView';
import FullScreenVideoPlayer from '../common/FullScreenVideoPlayer';

const EventStack = createStackNavigator({
    Events: EventsView,
    EventVideo: FullScreenVideoPlayer
  });
  
  EventStack.navigationOptions = ({ navigation }) => {
    let tabBarVisible = true;
    let routeName = navigation.state.routes[navigation.state.index].routeName
    if ( routeName == 'EventVideo' ) {
        tabBarVisible = false
    }
    return {
        tabBarVisible,
    }
}

export const EventTabRoute = {
    screen: EventStack,
    navigationOptions: {
        tabBarLabel: 'Events',
        tabBarIcon: tabBarIcon('ios-calendar')
    }
}