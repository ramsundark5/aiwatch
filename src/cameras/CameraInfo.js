import React, { Component } from 'react';
import { List } from 'react-native-paper';
import ConfigInput from './ConfigInput';
export default class CameraInfo extends Component {
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
      <List.Accordion title="Camera Info" expanded={state.expanded} onPress={this.toggleExpand}>
        <ConfigInput {...props} label="Camera Name" name="name" />

        <ConfigInput {...props} label="Camera Brand" name="brand" />

        <ConfigInput {...props} label="Camera Model" name="model" />
      </List.Accordion>
    );
  }
}
