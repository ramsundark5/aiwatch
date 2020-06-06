import React from 'react';
import { createStackNavigator } from 'react-navigation-stack';
import { tabBarIcon } from '../common/TabBarIcon';
import CameraView from './CameraView';
import EditCamera from './EditCamera';
import ScanCamera from './ScanCamera';
import RegionOfInterest from './RegionOfInterest';
import FullScreenVideoPlayer from '../common/FullScreenVideoPlayer';

const CameraStack = createStackNavigator({
    CameraView: CameraView,
    EditCamera: EditCamera,
    ScanCamera: ScanCamera,
    FullScreenVideo: FullScreenVideoPlayer,
    RegionOfInterest: RegionOfInterest
});
  
CameraStack.navigationOptions = ({ navigation }) => {
    let tabBarVisible = true;
    let stateParams = navigation.state.routes[navigation.state.index].params
    let fullScreenStatus = stateParams ? stateParams.fullscreen : false
    if ( fullScreenStatus) {
        tabBarVisible = false
    }
    return {
        tabBarVisible,
    }
}

export const CameraTabRoute = {
    screen: CameraStack,
    navigationOptions: {
        tabBarLabel: 'Camera',
        tabBarIcon: tabBarIcon('video')
    }
}