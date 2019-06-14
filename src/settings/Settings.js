import React, { Component } from 'react';
import { View } from 'react-native';
import RNSmartCam from '../native/RNSmartCam';
import { List, Switch } from 'react-native-paper';
import { withNavigation } from 'react-navigation';
import Spinner from 'react-native-loading-spinner-overlay';
import { updateSettings } from '../store/SettingsStore';
import GoogleConnectStatus from './GoogleConnectStatus';
import { connect } from 'react-redux';
class Settings extends Component{

    static navigationOptions = {
      headerTitle: 'Settings',
      headerTintColor: Theme.primary,
      headerTitleStyle: {
        fontSize: 16,
        fontWeight: 'normal',
      }
    };

    componentDidMount(){
      const { navigation } = this.props;
      this.loadSettings();
      this.focusListener = navigation.addListener('didFocus', () => this.loadSettings());
    }

    componentWillUnmount() {
      // Remove the event listener
      this.focusListener.remove();
    }

    componentDidUpdate(prevProps){
      const prevSettings  = prevProps.settings;
      const currentSettings = this.props.settings;
      if (currentSettings.isGoogleAccountConnected !== prevSettings.isGoogleAccountConnected 
        || currentSettings.isNotificationEnabled !== prevSettings.isNotificationEnabled 
        || currentSettings.isNoAdsPurchased !== prevSettings.isNoAdsPurchased ) {
          RNSmartCam.putSettings(this.props.settings);
      }
    }

    async loadSettings(){
      const { updateSettings } = this.props;
      let settings = await RNSmartCam.getSettings();
      let isMonitoringRunning = await RNSmartCam.isMonitoringServiceRunning();
      settings.isMonitoringOn = isMonitoringRunning;
      updateSettings(settings);
    }

    async onNotificationEnabledChange(value){
      const { updateSettings } = this.props;
      updateSettings({ isLoading: true });
      try{
        updateSettings({ notificationEnabled: value });
      }finally{
        updateSettings({ isLoading: false });
      }
    }

    async onToggleMonitoring(enableMonitoring){
      const { updateSettings } = this.props;
      updateSettings({ isLoading: true });
      try{
        await RNSmartCam.toggleMonitoringStatus(enableMonitoring);
        updateSettings({ isMonitoringOn: enableMonitoring });
      }finally{
        updateSettings({ isLoading: false });
      }
    }

    render(){
      const { isLoading } = this.props;
      return(
          <View>
            <Spinner
              visible={isLoading}
              textContent={'Loading...'} />
            <List.Section>
              <List.Item title="Store in Google Drive" right={() => this.renderGoogleAccountConnected()} />
              <List.Item title="Enable Notification" right={() => this.renderNotificationEnabled()} />
              <List.Item title="Monitoring Service Running" right={() => this.renderMonitoringEnabled()} />
            </List.Section>
          </View>
      );
  }

  renderGoogleAccountConnected() {
    const { settings, updateSettings } = this.props;
    return (
      <GoogleConnectStatus isGoogleAccountConnected={settings.isGoogleAccountConnected}
          updateSettings={updateSettings}/>
    );
  }

  renderNotificationEnabled() {
    const { settings } = this.props;
    return (
      <Switch
        value={settings.notificationEnabled}
        onValueChange={value => this.onNotificationEnabledChange(value)}
      />
    );
  }

  renderMonitoringEnabled() {
    const { isMonitoringOn } = this.props.settings;
    return (
      <Switch
        value={isMonitoringOn}
        onValueChange={value => this.onToggleMonitoring(value)}
      />
    );
  }
}
const mapStateToProps = state => ({
  settings: state.settings
});

const connectedSettings = connect(
  mapStateToProps,
  { updateSettings }
)(Settings);

export default withNavigation(connectedSettings);