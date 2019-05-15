import React, { Component } from 'react';
import { List } from 'react-native-paper';
import ConfigInput from './ConfigInput';
export default class ConnectionInfo extends Component {
  state = {
    expanded: true
  };
  toggleExpand = () => {
    this.setState({
      expanded: !this.state.expanded
    });
  };

  render() {
    const { props, state } = this;
    return (
      <List.Accordion title="Connection Info" expanded={state.expanded} onPress={this.toggleExpand}>
        <ConfigInput {...props} label="Video URL" name="videoUrl" />

        <ConfigInput {...props} label="Username" name="username" />

        <ConfigInput {...props} label="Password" name="password" />
      </List.Accordion>
    );
  }
}
