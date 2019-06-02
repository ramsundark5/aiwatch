/** @format */

import {AppRegistry, InteractionManager} from 'react-native';
import App from './App';
import {name as appName} from './app.json';
import BackgroundListener from './src/events/BackgroundListener';
import { Client } from 'bugsnag-react-native';

const bugsnag = new Client("e9bfebda8f0ae93852a57dd0e632fb52");
AppRegistry.registerComponent(appName, () => App);

InteractionManager.runAfterInteractions(() => {
    BackgroundListener.init();
});
