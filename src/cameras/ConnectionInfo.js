import React, { Component } from 'react';
import { Button, List } from 'react-native-paper';
import ConfigInput from './ConfigInput';
import Theme from '../common/Theme';
export default class ConnectionInfo extends Component {
  
  state = {
    expanded: true
  };

  toggleExpand = () => {
    this.setState({
      expanded: !this.state.expanded
    });
  };

  testConnection(){

  }

  render() {
    const { props, state } = this;
    return (
      <List.Accordion title="Connection Info" expanded={state.expanded} onPress={this.toggleExpand}>
        <ConfigInput {...props} label="Video URL" name="videoUrl" />
        <Button mode='outlined' color={Theme.primary} onPress={() => this.testConnection()}>
          Test Connection
        </Button>
        <ConfigInput {...props} label="Username" name="username" />

        <ConfigInput {...props} label="Password" name="password" />
      </List.Accordion>
    );
  }
}
