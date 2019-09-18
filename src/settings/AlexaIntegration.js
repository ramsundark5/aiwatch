import React, { Component } from 'react';
import { Linking, ToastAndroid, View } from 'react-native';
import { Colors, List, Switch, Text } from 'react-native-paper';
import EditableText from '../common/EditableText';

export default class AlexaIntegration extends Component{
    async onUpdateAlexaToken(token){
      const { updateSettings } = this.props;
      updateSettings({ alexaToken: token });
    }

    onChangeConnectStatus(connectAlexa){
      const { alexaToken, updateSettings } = this.props;
      let isAlexaTokenValid = alexaToken && alexaToken.length > 5 ? true : false ;
      if(connectAlexa && !isAlexaTokenValid){
        ToastAndroid.showWithGravity('Valid Notify Me token required to enable Alexa integration.', ToastAndroid.SHORT, ToastAndroid.CENTER);
        return;
      }
      updateSettings({ isAlexaConnected: connectAlexa });
    }

    render(){
      return (
        <View>
            <List.Item title="Notify Alexa"
                description="Notify Alexa on interested alerts"
                right={() => this.renderSwitch()} />
            <Text style={{fontWeight: 'bold', paddingLeft: 20, color: Colors.blue500}}
                    onPress={() => Linking.openURL('https://aiwatch.live/alexa.html')}>
                View setup instructions
            </Text>
            {this.renderAlexaTokenInput()}
        </View>
      );
    }

    renderSwitch(){
      const { isAlexaConnected } = this.props;
      return(
        <Switch
          value={isAlexaConnected}
          onValueChange={value => this.onChangeConnectStatus(value)}
        />
      )
    }
    renderAlexaTokenInput(){
      const { alexaToken } = this.props;
      return(
        <View>
          <EditableText 
              mask={true}
              editable={true}
              label='Notify Me Token'
              textContent={alexaToken}
              finishEditText={(token) => this.onUpdateAlexaToken(token)}/>
        </View>
      )
    }
}
