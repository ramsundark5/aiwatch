import React, { Component } from 'react';
import { View } from 'react-native';
import { Switch } from 'react-native-paper';
import DialogInput from '../common/DialogInput';

export default class AlexaIntegration extends Component{

    state = {
      showTokenInput: false,
    }

    onChangeConnectStatus(requestConnect){
      const { updateSettings } = this.props;

      if(!requestConnect){
        //remove access tokens from local db
        updateSettings({ alexaToken: null });
      }else{
        this.showAlexaTokenInput(true);
      }
    }

    showAlexaTokenInput(isShow){
      this.setState({showTokenInput: isShow});
    }
    
    async onConnectAlexa(token){
      const { updateSettings } = this.props;
      updateSettings({ alexaToken: token });
      this.showAlexaTokenInput(false);
    }

    render(){
      const { alexaToken } = this.props;
      let isAlexaConnected = alexaToken ? true : false ;
      return (
        <View style={{paddingTop: 10}}>
          <Switch
            value={isAlexaConnected}
            onValueChange={value => this.onChangeConnectStatus(value)}
          />
          {this.renderTokenInputDialog(alexaToken)}
        </View>
      );
    }

    renderTokenInputDialog(alexaToken){
      return(
        <DialogInput isDialogVisible={this.state.showTokenInput}
          title={"Connect Alexa"}
          message={"Enter the token from Alexa Notify me skill"}
          hintInput ={alexaToken}
          submitInput={ (inputText) => {this.onConnectAlexa(inputText)} }
          closeDialog={ () => {this.showAlexaTokenInput(false)}} />
      )
    }
}
