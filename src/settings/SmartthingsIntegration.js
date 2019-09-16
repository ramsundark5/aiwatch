import React, { Component } from 'react';
import { authorize } from 'react-native-app-auth';
import { ToastAndroid, View } from 'react-native';
import { List, Switch } from 'react-native-paper';
import EditableText from '../common/EditableText';
import Logger from '../common/Logger';
import RNSmartCam from '../native/RNSmartCam';

export default class SmartthingsIntegration extends Component{

    componentDidCatch(err){
      console.log(err);
    }

    onChangeConnectStatus(requestConnect){
      const { updateSettings } = this.props;

      if(!requestConnect){
        //remove access tokens from local db
        updateSettings({ smartthingsAccessToken: null, smartthingsAccessTokenExpiry: null });
        ToastAndroid.showWithGravity('Smartthings integration disconnected.', ToastAndroid.SHORT, ToastAndroid.CENTER);
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
        ToastAndroid.showWithGravity('Smartthings integration was successfull.', ToastAndroid.SHORT, ToastAndroid.CENTER);
        updateSettings(updatedSettings);
        console.log('smartthings token saved successfully');
      }catch(err){
        Logger.log('error saving smartthings token' + err);
      }finally{
        updateSettings({ isLoading: false });
      }
    }

    async getOauthToken(){
        const { smartthingsClientId, smartthingsClientSecret } = this.props.settings;
        try {
            console.log('starting authorize');
            // base config
            const config = {
              serviceConfiguration:{
                authorizationEndpoint: 'https://graph.api.smartthings.com/oauth/authorize',
                tokenEndpoint: 'https://graph.api.smartthings.com/oauth/token'
              },
              clientId: smartthingsClientId,
              clientSecret: smartthingsClientSecret,
              redirectUrl: 'com.aiwatch.oauth:/oauthredirect',
              scopes: ['app'],
            };
            const result = await authorize(config);
            console.log('smartthings token '+JSON.stringify(result));
            return result;
            // result includes accessToken, accessTokenExpirationDate and refreshToken
        } catch (error) {
            ToastAndroid.showWithGravity('Error connecting to smartthings. Check if you have valid ClientId and ClientSecret', ToastAndroid.SHORT, ToastAndroid.CENTER);
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

    updateSmartthingsClientId(value){
      const { updateSettings } = this.props;
      updateSettings({ smartthingsClientId: value });
    }

    updateSmartthingsClientSecret(value){
      const { updateSettings } = this.props;
      updateSettings({ smartthingsClientSecret: value });
    }

    render(){
      return (
        <View>
          <List.Item title="Connect Smartthings"
                  description="Integrate with your smartthings hub"
                  right={() => this.renderSwitch()} />
          {this.renderOAuthCredentials()}
        </View>
      );
    }

    renderSwitch(){
      const { smartthingsAccessToken } = this.props.settings;
      let isSmartthingsConnected = smartthingsAccessToken ? true : false ;
      return(
        <Switch
            value={isSmartthingsConnected}
            onValueChange={value => this.onChangeConnectStatus(value)}/>
      )
    }

    renderOAuthCredentials(){
      const { smartthingsClientId, smartthingsClientSecret } = this.props.settings;
      return(
        <View>
          <EditableText 
              editable={true}
              label='Client Id'
              textContent={smartthingsClientId}
              finishEditText={(clientId) => this.updateSmartthingsClientId(clientId)}/>
          <EditableText 
              editable={true}
              mask={true}
              label='Client Secret'
              textContent={smartthingsClientSecret}
              finishEditText={(clientSecret) => this.updateSmartthingsClientSecret(clientSecret)}/>
        </View>
      )
    }
}
