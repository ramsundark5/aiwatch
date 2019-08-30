import React, { Component } from 'react';
import { View } from 'react-native';
import RNSmartCam from '../native/RNSmartCam';
import { List, Switch, Button } from 'react-native-paper';
import Theme from '../common/Theme';
import { withNavigation } from 'react-navigation';
import Spinner from 'react-native-loading-spinner-overlay';
import { updateSettings } from '../store/SettingsStore';
import GoogleConnectStatus from './GoogleConnectStatus';
import { connect } from 'react-redux';
import InAppPurchase from './InAppPurchase';
import {LogView} from 'react-native-device-log';
import Logger from '../common/Logger';
import SmartthingsIntegration from './SmartthingsIntegration';

class Settings extends Component{

    state = {
      syncing: false
    }

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

    async componentDidUpdate(prevProps){
      const prevSettings  = prevProps.settings;
      const currentSettings = this.props.settings;
      if (currentSettings.isGoogleAccountConnected !== prevSettings.isGoogleAccountConnected 
        || currentSettings.isNotificationEnabled !== prevSettings.isNotificationEnabled 
        || currentSettings.isNoAdsPurchased !== prevSettings.isNoAdsPurchased
        || currentSettings.smartthingsAccessToken !== prevSettings.smartthingsAccessToken
        || currentSettings.smartthingsAccessTokenExpiry !== prevSettings.smartthingsAccessTokenExpiry ) {
          try{
            let updatedSettings = await RNSmartCam.putSettings(currentSettings);
            console.log('updatedSettings after save ' + JSON.stringify(updatedSettings));
          }catch(err){
            console.log('Error saving settings to db ' + err);
          }
      }
    }

    async loadSettings(){
      const { updateSettings } = this.props;
      let settings = await RNSmartCam.getSettings();
      console.log('settings from db '+ JSON.stringify(settings));
      updateSettings(settings);
    }

    async onNotificationEnabledChange(value){
      const { updateSettings } = this.props;
      updateSettings({ isLoading: true });
      try{
        updateSettings({ isNotificationEnabled: value });
      }finally{
        updateSettings({ isLoading: false });
      }
    }

    onShowDeviceLogsChange(value){
      const { updateSettings } = this.props;
      updateSettings({ showDeviceLogs: value });
    }

    async onSyncPress(){
      try{
        this.setState({syncing: true});
        await RNSmartCam.sync();
      }catch(err){
        Logger.log('error sycing to firebase' + err);
      }finally{
        this.setState({syncing: false});
      }
    }

    async onConnectSmartthimgs(){
      const { updateSettings } = this.props;
      updateSettings({ isLoading: true });
      try{
        const result = await SmartthingsIntegration.getOauthToken();
        let updatedSettings = await RNSmartCam.saveSmartthingsAccessToken(result);
        updateSettings(updatedSettings);
        console.log('smartthings token saved successfully');
      }catch(err){
        Logger.log('error saving smartthings token' + err);
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
              <List.Item title="Enable cloud sync" 
                  description="Required for remote viewing and notification"
                  right={() => this.renderGoogleAccountConnected()} />
              <List.Item title="Enable Notification"
                  right={() => this.renderNotificationEnabled()} />
              <List.Item title="Show Device Logs"
                  description="Required for troubleshooting purpose"
                  right={() => this.renderDeviceLogsEnabled()} />
            </List.Section>
            {this.renderSyncButton()}
            {this.renderSmartthingsButton()}
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
        value={settings.isNotificationEnabled}
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

  renderSyncButton(){
    const { settings } = this.props;
    const { syncing } = this.state;
    if(!settings.isGoogleAccountConnected){
      return null;
    }
    return(
      <Button style={{marginLeft: 30, marginRight: 30}} mode='outlined' color={Theme.primary} loading={syncing} onPress={() => this.onSyncPress()}>
        Sync Configs
      </Button>
    )
  }

  renderSmartthingsButton(){
    return(
      <Button style={{marginLeft: 30, marginRight: 30}} mode='outlined' color={Theme.primary} onPress={() => this.onConnectSmartthimgs()}>
        Connect Smartthings
      </Button>
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