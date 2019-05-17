import React from 'react';
import { createStackNavigator } from 'react-navigation';
import { tabBarIcon } from '../common/TabBarIcon';
import Settings from './Settings';

const SettingsStack = createStackNavigator({
    Settings: Settings
});
  
export const SettingsTabRoute = {
    screen: SettingsStack,
    navigationOptions: {
        tabBarLabel: 'Settings',
        tabBarIcon: tabBarIcon('ios-settings')
    }
}