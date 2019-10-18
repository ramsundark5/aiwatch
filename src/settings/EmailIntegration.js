import React, { Component } from 'react';
import { ToastAndroid, View } from 'react-native';
import { List, Switch } from 'react-native-paper';
import EditableText from '../common/EditableText';

export default class EmailIntegration extends Component{
    async onUpdateEmailUsername(username){
      const { updateSettings } = this.props;
      updateSettings({ emailUsername: username });
    }

    async onUpdateEmailPassword(password){
        const { updateSettings } = this.props;
        updateSettings({ emailPassword: password });
    }

    async onUpdateReceiverEmailUsername(recevierusername){
        const { updateSettings } = this.props;
        updateSettings({ receiverEmailUsername: recevierusername });
    }

    onChangeEmailStatus(emailEnabled){
      const { updateSettings } = this.props;
      const { emailUsername, emailPassword, receiverEmailUsername } = this.props.settings;
      if(emailEnabled && (!emailUsername || !emailPassword || !receiverEmailUsername)){
        ToastAndroid.showWithGravity('Email username and password required.', ToastAndroid.SHORT, ToastAndroid.CENTER);
        return;
      }
      updateSettings({ isEmailEnabled: emailEnabled });
    }

    render(){
      return (
        <View>
            <List.Item title="Email notification"
                description="Notify events via email"
                right={() => this.renderSwitch()} />
            {this.renderEmailInput()}
        </View>
      );
    }

    renderSwitch(){
      const { isEmailEnabled } = this.props.settings;
      return(
        <Switch
          value={isEmailEnabled}
          onValueChange={value => this.onChangeEmailStatus(value)}
        />
      )
    }
    renderEmailInput(){
      const { emailUsername, emailPassword, receiverEmailUsername } = this.props.settings;
      return(
        <View>
            <EditableText 
              mask={false}
              editable={true}
              label='From email address'
              textContent={emailUsername}
              finishEditText={(username) => this.onUpdateEmailUsername(username)}/>
            <EditableText 
              secureTextEntry={true}
              editable={true}
              label='From email password'
              textContent={emailPassword}
              finishEditText={(password) => this.onUpdateEmailPassword(password)}/>
            <EditableText 
              mask={false}
              editable={true}
              label='To email address'
              textContent={receiverEmailUsername}
              finishEditText={(recevierusername) => this.onUpdateReceiverEmailUsername(recevierusername)}/>  
        </View>
      )
    }
}
