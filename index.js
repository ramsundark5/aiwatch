/** @format */
import Logger from './src/common/Logger';
import {AppRegistry, InteractionManager} from 'react-native';
import App from './App';
import {name as appName} from './app.json';
import BackgroundListener from './src/events/BackgroundListener';

AppRegistry.registerComponent(appName, () => App);

InteractionManager.runAfterInteractions(() => {
    BackgroundListener.init();
    Logger.log('app initialized');
});
