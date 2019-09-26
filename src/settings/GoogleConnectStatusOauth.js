import React, { Component } from 'react';
import { authorize, revoke } from 'react-native-app-auth';
import { Switch } from 'react-native-paper';
import { ToastAndroid } from 'react-native';
import Logger from '../common/Logger';

//webClientId: '119466713568-o59oc7i1d9vr7blopd1396jnhs6cudtn.apps.googleusercontent.com', // client ID of type WEB for your server (needed to verify user ID and offline access)
const config = {
  issuer: 'https://accounts.google.com',
  clientId: '119466713568-8eiocl6rns75ab9sdno2r60psa03jdfk.apps.googleusercontent.com',
  redirectUrl: 'com.googleusercontent.apps.119466713568-8eiocl6rns75ab9sdno2r60psa03jdfk:/oauth2redirect/google',
  scopes: ['openid', 'profile','https://www.googleapis.com/auth/drive.file']
};

export default class GoogleConnectStatus extends Component{ 
  
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
          // Log in to get an authentication token
          const authState = await authorize(config);
          console.log('google auth ' + authState);
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
          await revoke(config, {
            tokenToRevoke: refreshedState.refreshToken
          });
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