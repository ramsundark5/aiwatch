import React, { Component } from 'react';
import { GoogleSignin, statusCodes } from 'react-native-google-signin';
import { Switch } from 'react-native-paper';
import Logger from '../common/Logger';

export default class GoogleConnectStatus extends Component{ 
  
    componentDidMount(){
        GoogleSignin.configure({
          scopes: ['https://www.googleapis.com/auth/drive.file'], // what API you want to access on behalf of the user, default is email and profile
          webClientId: '119466713568-o59oc7i1d9vr7blopd1396jnhs6cudtn.apps.googleusercontent.com', // client ID of type WEB for your server (needed to verify user ID and offline access)
          offlineAccess: false, // if you want to access Google API on behalf of the user FROM YOUR SERVER
        });
    }
  
    async onGoogleAccountSettingsChange(isConnected){
        const { updateSettings } = this.props;
        updateSettings({ isLoading: true });
        try{
            if(isConnected){
              await this.connectGoogleAccount();
            }else{
              await this.disconnectGoogleAccount();
            }
        }catch(err){
          Logger.error(err);
        }finally{
          updateSettings({ isLoading: false });
        }
    }
  
    async connectGoogleAccount(){
        const { updateSettings } = this.props;
        try {
            await GoogleSignin.signIn();
            updateSettings({ isGoogleAccountConnected: true });
        }catch (error) {
            updateSettings({ isGoogleAccountConnected: false });
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
        const { updateSettings } = this.props;
        try{
          await GoogleSignin.revokeAccess();
          updateSettings({ isGoogleAccountConnected: false });
        }catch(err){
          Logger.log(err);
          if(err.message ==="SIGN_IN_REQUIRED"){
            updateSettings({ isGoogleAccountConnected: false });
          }
        }
    }

    render(){
      const { isGoogleAccountConnected } = this.props;
        return (
            <Switch
              value={isGoogleAccountConnected}
              onValueChange={value => this.onGoogleAccountSettingsChange(value)}
            />
        );
    }
}