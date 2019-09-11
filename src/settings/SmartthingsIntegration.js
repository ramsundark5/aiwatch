import React, { Component } from 'react';
import { authorize } from 'react-native-app-auth';
import { View } from 'react-native';
import { Switch } from 'react-native-paper';
import EditableText from '../common/EditableText';
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

    async getSmartAppEndpoint(authData){
      var url = 'https://graph.api.smartthings.com/api/smartapps/endpoints';
      try{
        const bearer = 'Bearer ' + authData.accessToken;
        const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Authorization': bearer
                }
        });
        const jsonBody = await response.json();
        console.log('smartapp endpoint response is '+JSON.stringify(jsonBody));
        const endpoint = jsonBody[0].uri;
        console.log('smartapp endpoint is '+endpoint);
        return endpoint;
      }catch(err){
        console.log('error getting smartapp endpoint '+err);
        return null;
      }
    }

    render(){
      const { smartthingsAccessToken } = this.props;
      let isSmartthingsConnected = smartthingsAccessToken ? true : false ;
      return (
        <View>
          <Switch
            value={isSmartthingsConnected}
            onValueChange={value => this.onChangeConnectStatus(value)}/>
          {this.renderOAuthCredentials()}
        </View>
      );
    }

    renderOAuthCredentials(){
      return(
        <View>
          <EditableText 
              editable={true}
              textContent=''
              finishEditText={(finishedText) => console.log(finishedText)}/>
          <EditableText 
              editable={true}
              textContent=''
              finishEditText={(finishedText) => console.log(finishedText)}/>
        </View>
      )
    }
}
