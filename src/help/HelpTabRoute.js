import React, { Component } from 'react';
import { Text, View } from 'react-native';
//import { WebView } from 'react-native-webview';
import { createStackNavigator } from 'react-navigation-stack';
import { tabBarIcon } from '../common/TabBarIcon';
import HelpScreen from './HelpScreen';

const HelpStack = createStackNavigator({
    Help: HelpScreen
});

export const HelpTabRoute = {
    screen: HelpStack,
    navigationOptions: {
        tabBarLabel: 'FAQ',
        tabBarIcon: tabBarIcon('help-circle-outline')
    }
}
