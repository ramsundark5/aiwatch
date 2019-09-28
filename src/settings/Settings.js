import React, { Component } from 'react';
import { Text, View } from 'react-native';
import RNSmartCam from '../native/RNSmartCam';
import { Button, Colors, List, Switch } from 'react-native-paper';
import Theme from '../common/Theme';
import { withNavigation } from 'react-navigation';
import LoadingSpinner from '../common/LoadingSpinner';
import { updateSettings } from '../store/SettingsStore';
import GoogleConnectStatusOauth from './GoogleConnectStatusOauth';
import { connect } from 'react-redux';
import InAppPurchase from './InAppPurchase';
import {LogView} from 'react-native-device-log';
import Logger from '../common/Logger';
import SmartthingsIntegration from './SmartthingsIntegration';
import AlexaIntegration from './AlexaIntegration';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
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

    UNSAFE_componentWillUnmount() {
      // Remove the event listener
      this.focusListener.remove();
    }
    
    async componentDidUpdate(prevProps){
      console.log('component updating');
      const prevSettings  = prevProps.settings;
      const currentSettings = this.props.settings;
      if (currentSettings.isGoogleAccountConnected !== prevSettings.isGoogleAccountConnected 
        || currentSettings.isNotificationEnabled !== prevSettings.isNotificationEnabled 
        || currentSettings.isNoAdsPurchased !== prevSettings.isNoAdsPurchased
        || currentSettings.smartthingsAccessToken !== prevSettings.smartthingsAccessToken
        || currentSettings.smartthingsAccessTokenExpiry !== prevSettings.smartthingsAccessTokenExpiry 
        || currentSettings.smartAppEndpoint !== prevSettings.smartAppEndpoint
        || currentSettings.alexaToken !== prevSettings.alexaToken
        || currentSettings.smartthingsClientId !== prevSettings.smartthingsClientId
        || currentSettings.smartthingsClientSecret !== prevSettings.smartthingsClientSecret
        || currentSettings.isAlexaConnected !== prevSettings.isAlexaConnected
        || currentSettings.isExternalStorageEnabled !== prevSettings.isExternalStorageEnabled
        || currentSettings.googleAccessToken !== prevSettings.googleAccessToken
        || currentSettings.googleRefreshToken !== prevSettings.googleRefreshToken ) {
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

    onNotificationEnabledChange(value){
      const { updateSettings } = this.props;
      updateSettings({ isLoading: true });
      try{
        updateSettings({ isNotificationEnabled: value });
      }finally{
        updateSettings({ isLoading: false });
      }
    }

    onExternalStorageEnabledChange(value){
        const { updateSettings } = this.props;
        updateSettings({ isLoading: true });
        try{
          updateSettings({ isExternalStorageEnabled: value });
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
        Logger.log('Error sycing to firebase' + err);
      }finally{
        this.setState({syncing: false});
      }
    }

    render(){
      const { settings } = this.props;
      const isLoading = settings ? settings.isLoading : false;
      return(
          <KeyboardAwareScrollView>
            <LoadingSpinner
              visible={isLoading}
              textContent={'Loading...'} />
            <List.Section>
              <List.Item title="Enable cloud sync" 
                  description="Required for remote viewing and notification"
                  right={() => this.renderGoogleAccountConnected()} />
              <List.Item title="Enable Notification"
                  right={() => this.renderNotificationEnabled()} />
              <List.Item title="Use External Storage"
                  right={() => this.renderExternalStorageEnabled()} />
              {this.renderSmartthingsEnabled()}
              {this.renderAlexaEnabled()}
              <List.Item title="Show Device Logs"
                  description="Required for troubleshooting purpose"
                  right={() => this.renderDeviceLogsEnabled()} />
            </List.Section>
            {this.renderSyncButton()}
            <InAppPurchase {...this.props}/>
            {this.renderDeviceLogs()}
          </KeyboardAwareScrollView>
      );
  }

  renderGoogleAccountConnected() {
    const { settings, updateSettings } = this.props;
    if(!settings.isNoAdsPurchased){
      return this.renderPremiumRequired();
    }
    return (
      <GoogleConnectStatusOauth settings={settings}
          updateSettings={updateSettings}/>
    );
  }

  renderNotificationEnabled() {
    const { settings } = this.props;
    if(!settings.isNoAdsPurchased){
      return this.renderPremiumRequired();
    }
    return (
      <Switch
        value={settings.isNotificationEnabled}
        onValueChange={value => this.onNotificationEnabledChange(value)}
      />
    );
  }

  renderSmartthingsEnabled(){
    const { settings, updateSettings } = this.props;
    return (
      <SmartthingsIntegration settings={settings}
          updateSettings={updateSettings}/>
    );
  }

  renderAlexaEnabled(){
    const { settings, updateSettings } = this.props;
    return (
      <AlexaIntegration alexaToken={settings.alexaToken} isAlexaConnected={settings.isAlexaConnected}
          updateSettings={updateSettings}/>
    );
  }

  renderExternalStorageEnabled() {
    const { settings } = this.props;
    return (
      <Switch
        value={settings.isExternalStorageEnabled}
        onValueChange={value => this.onExternalStorageEnabledChange(value)}
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

  renderPremiumRequired(){
    return(
      <View style={{justifyContent: 'center'}}>
        <Text style={{color: Colors.white, backgroundColor: Colors.blue300, paddingLeft: 5, paddingRight: 5, borderRadius: 5}}>Premium</Text>
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