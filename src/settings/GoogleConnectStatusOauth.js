import React, { Component } from 'react';
import { authorize, refresh, revoke } from 'react-native-app-auth';
import { Switch } from 'react-native-paper';
import { ToastAndroid } from 'react-native';
import RNSmartCam from '../native/RNSmartCam';
import Logger from '../common/Logger';

//webClientId: '119466713568-o59oc7i1d9vr7blopd1396jnhs6cudtn.apps.googleusercontent.com', // client ID of type WEB for your server (needed to verify user ID and offline access)
const config = {
  issuer: 'https://accounts.google.com',
  clientId: '119466713568-8eiocl6rns75ab9sdno2r60psa03jdfk.apps.googleusercontent.com',
  redirectUrl: 'com.googleusercontent.apps.119466713568-8eiocl6rns75ab9sdno2r60psa03jdfk:/oauth2redirect/google',
  scopes: ['openid', 'profile','https://www.googleapis.com/auth/drive.file']
};

export default class GoogleConnectStatusOauth extends Component{ 
  
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
          console.log('refresh token is '+authState.refreshToken);
          updateSettings({ 
            isGoogleAccountConnected: true, 
            googleAccessToken:  authState.accessToken,
            googleRefreshToken: authState.refreshToken
          });
        }catch (error) {
            updateSettings({ isGoogleAccountConnected: false });
            ToastAndroid.show("Error connecting to your Google account. Try again.", ToastAndroid.SHORT);
        }
    }
  
    async disconnectGoogleAccount(){
        const { updateSettings, googleRefreshToken } = this.props;
        try{
          await revoke(config, {
            tokenToRevoke: googleRefreshToken
          });
          updateSettings({ 
            isGoogleAccountConnected: false,
            googleAccessToken:  null,
            googleRefreshToken: null 
          });
        }catch(err){
          Logger.log(err);
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