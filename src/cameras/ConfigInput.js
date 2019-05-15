import React, { Component } from 'react';
import { TextInput } from 'react-native-paper';

export default class ConfigInput extends Component {
  onValueChange(value) {
    const { name, onConfigChange } = this.props;
    onConfigChange(name, value);
  }

  render() {
    const { props } = this;
    const { cameraConfig, name, label } = props;
    const configValue = cameraConfig[name];
    return (
      <TextInput
        {...props}
        style={{ backgroundColor: 'white' }}
        label={label}
        name={name}
        value={configValue}
        onChangeText={text => this.onValueChange(text)}
      />
    );
  }
}
