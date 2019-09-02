import React, { Component } from 'react';
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
        updateSettings({ alexaToken: null });
      }else{
        this.onConnectAlexa();
      }
    }

    async onConnectAlexa(){
      const { updateSettings } = this.props;
      updateSettings({ isLoading: true });
      try{
        let result = await this.getOauthToken();
        const smartAppEndpoint = await this.getSmartAppEndpoint(result);
        result['smartAppEndpoint'] = smartAppEndpoint;
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
