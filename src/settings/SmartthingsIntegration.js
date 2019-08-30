import React, { Component } from 'react';
import { authorize } from 'react-native-app-auth';
import { Switch } from 'react-native-paper';
import Logger from '../common/Logger';
import RNSmartCam from '../native/RNSmartCam';

// base config
const config = {
  serviceConfiguration:{
    authorizationEndpoint: 'https://graph.api.smartthings.com/oauth/authorize',
    tokenEndpoint: 'https://graph.api.smartthings.com/oauth/token'
  },
  clientId: '5c9baee2-daa5-46ff-abc0-2cdceeb284ea',
  clientSecret: 'f6a70c01-410d-46dd-95a0-94b7ad76f8b2',
  redirectUrl: 'com.aiwatch.oauth:/oauthredirect',
  scopes: ['app'],
};

export default class SmartthingsIntegration extends Component{

    onChangeConnectStatus(requestConnect){
      const { updateSettings } = this.props;

      if(!requestConnect){
        //remove access tokens from local db
        updateSettings({ smartthingsAccessToken: null, smartthingsAccessTokenExpiry: null });
      }else{
        this.onConnectSmartthimgs();
      }
    }

    async onConnectSmartthimgs(){
      const { updateSettings } = this.props;
      updateSettings({ isLoading: true });
      try{
        const result = await this.getOauthToken();
        let updatedSettings = await RNSmartCam.saveSmartthingsAccessToken(result);
        updateSettings(updatedSettings);
        console.log('smartthings token saved successfully');
      }catch(err){
        Logger.log('error saving smartthings token' + err);
      }finally{
        updateSettings({ isLoading: false });
      }
    }

    async getOauthToken(){
        try {
            console.log('starting authorize');
            const result = await authorize(config);
            console.log('smartthings token '+JSON.stringify(result));
            return result;
            // result includes accessToken, accessTokenExpirationDate and refreshToken
        } catch (error) {
            console.log(error);
        }
    }

    render(){
      const { smartthingsAccessToken } = this.props;
      let isSmartthingsConnected = smartthingsAccessToken ? true : false ;
      return (
        <Switch
          value={isSmartthingsConnected}
          onValueChange={value => this.onChangeConnectStatus(value)}
        />
      );
    }
}
