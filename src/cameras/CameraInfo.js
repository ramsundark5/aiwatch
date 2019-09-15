import React, { Component } from 'react';
import { List } from 'react-native-paper';
import ConfigInput from './ConfigInput';
import testID from '../common/testID';
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
        <ConfigInput {...props} label="Camera Name" name="name" {...testID('CAMERA_NAME')}/>

        <ConfigInput {...props} label="Camera Brand" name="brand" {...testID('CAMERA_BRAND')}/>

        <ConfigInput {...props} label="Camera Model" name="model" {...testID('CAMERA_MODEL')}/>
      </List.Accordion>
    );
  }
}
