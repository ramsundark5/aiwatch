import React, { Component } from 'react';
import { View } from 'react-native';
import RNSmartCam from '../native/RNSmartCam';
import { List, Switch } from 'react-native-paper';
import { withNavigation } from 'react-navigation';
import Spinner from 'react-native-loading-spinner-overlay';
import { updateSettings } from '../store/SettingsStore';
import GoogleConnectStatus from './GoogleConnectStatus';
import { connect } from 'react-redux';
import InAppPurchase from './InAppPurchase';
import {LogView} from 'react-native-device-log';
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
      //this.loadSettings();
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

    onShowDeviceLogsChange(value){
      const { updateSettings } = this.props;
      updateSettings({ showDeviceLogs: value });
    }

    render(){
      const { isLoading } = this.props;
      return(
          <View>
            <Spinner
              visible={isLoading}
              textContent={'Loading...'} />
            <List.Section>
              <List.Item title="Enable cloud sync" 
                  description="Required for remote viewing and notification"
                  right={() => this.renderGoogleAccountConnected()} />
              <List.Item title="Enable Notification"
                  right={() => this.renderNotificationEnabled()} />
              <List.Item title="Show Device Logs"
                  description="Required for troubleshooting purpose"
                  right={() => this.renderDeviceLogsEnabled()} />
            </List.Section>
            <InAppPurchase {...this.props}/>
            {this.renderDeviceLogs()}
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

  renderDeviceLogsEnabled() {
    const { settings } = this.props;
    return (
      <Switch
        value={settings.showDeviceLogs}
        onValueChange={value => this.onShowDeviceLogsChange(value)}
      />
    );
  }

  renderDeviceLogs(){
    const { settings } = this.props;
    if(!settings.showDeviceLogs){
      return null;
    }
    return(
      <View style={{height: 600, paddingTop: 30}}>
        <LogView inverted={false} multiExpanded={true} timeStampFormat='HH:mm:ss'></LogView>
      </View>
    )
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