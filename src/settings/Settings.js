import React, { Component } from 'react';
import { ToastAndroid, View } from 'react-native';
import { GoogleSignin, statusCodes } from 'react-native-google-signin';
import RNSmartCam from '../native/RNSmartCam';
import { List, Switch } from 'react-native-paper';
import { withNavigation } from "react-navigation";
class Settings extends Component{

    static navigationOptions = {
      headerTitle: 'Settings',
      headerTintColor: Theme.primary,
      headerTitleStyle: {
        fontSize: 16,
        fontWeight: 'normal',
      }
    };

    state = {
      settings: {},
      isMonitoringOn: false
    };

    componentDidMount(){
      const { navigation } = this.props;
      GoogleSignin.configure({
        scopes: ['https://www.googleapis.com/auth/drive.file'], // what API you want to access on behalf of the user, default is email and profile
        webClientId: '119466713568-o59oc7i1d9vr7blopd1396jnhs6cudtn.apps.googleusercontent.com', // client ID of type WEB for your server (needed to verify user ID and offline access)
        offlineAccess: false, // if you want to access Google API on behalf of the user FROM YOUR SERVER
      });
      this.loadSettings();
      this.focusListener = navigation.addListener('didFocus', () => this.loadSettings());
    }

    componentWillUnmount() {
      // Remove the event listener
      this.focusListener.remove();
    }

    async loadSettings(){
      let settings = await RNSmartCam.getSettings();
      let isMonitoringRunning = await RNSmartCam.isMonitoringServiceRunning();
      this.setState({
        settings: settings, 
        isMonitoringOn: isMonitoringRunning
      });
    }

    async onGoogleAccountSettingsChange(isConnected){
      requestAnimationFrame(async () => {
        if(isConnected){
          await this.connectGoogleAccount();
        }else{
          await this.disconnectGoogleAccount();
        }
        RNSmartCam.putSettings(this.state.settings);
      });
    }

    async connectGoogleAccount(){
        const { settings } = this.state;
        try {
            await GoogleSignin.signIn();
            this.setState({
              settings: Object.assign({}, settings, { isGoogleAccountConnected: true })
            });
        }catch (error) {
            this.setState({
              settings: Object.assign({}, settings, { isGoogleAccountConnected: false })
            });
            if (error.code === statusCodes.SIGN_IN_CANCELLED) {
              // user cancelled the login flow
            } else if (error.code === statusCodes.IN_PROGRESS) {
              // operation (f.e. sign in) is in progress already
            } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
              // play services not available or outdated
            } else {
              // some other error happened
              ToastAndroid.show(error.message, ToastAndroid.SHORT);
            }
          }
    }

    async disconnectGoogleAccount(){
      const { settings } = this.state;
      try{
        await GoogleSignin.revokeAccess();
      }catch(err){

      }
      this.setState({
        settings: Object.assign({}, settings, { isGoogleAccountConnected: false })
      });
    }

    async onNotificationEnabledChange(value){
      const { settings } = this.state;
      this.setState({
        settings: Object.assign({}, settings, { notificationEnabled: value })
      });
      RNSmartCam.putSettings(this.state.settings);
    }

    async onToggleMonitoring(enableMonitoring){
      this.setState({
        isMonitoringOn: enableMonitoring
      });
      RNSmartCam.toggleMonitoringStatus(enableMonitoring);
    }

    render(){
        return(
            <View>
              <List.Section title="Person Detected">
                <List.Item title="Store in Google Drive" right={() => this.renderGoogleAccountConnected()} />
                <List.Item title="Enable Notification" right={() => this.renderNotificationEnabled()} />
                <List.Item title="Monitoring Service Running" right={() => this.renderMonitoringEnabled()} />
              </List.Section>
            </View>
        );
  }

  renderGoogleAccountConnected() {
    const { settings } = this.state;
    return (
      <Switch
        value={settings.isGoogleAccountConnected}
        onValueChange={value => this.onGoogleAccountSettingsChange(value)}
      />
    );
  }

  renderNotificationEnabled() {
    const { settings } = this.state;
    return (
      <Switch
        value={settings.notificationEnabled}
        onValueChange={value => this.onNotificationEnabledChange(value)}
      />
    );
  }

  renderMonitoringEnabled() {
    const { isMonitoringOn } = this.state;
    return (
      <Switch
        value={isMonitoringOn}
        onValueChange={value => this.onToggleMonitoring(value)}
      />
    );
  }
}

export default withNavigation(Settings);