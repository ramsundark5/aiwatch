import React, { Component } from 'react';
import {Picker} from 'react-native';
import { List, Switch } from 'react-native-paper';
export default class MonitorInfo extends Component {

  state = {
    expanded: true
  };

  toggleExpand = () => {
    this.setState({
      expanded: !this.state.expanded
    });
  };

  render() {
    const { state } = this;
    return (
      <List.Accordion title='Motion Detection' expanded={state.expanded} onPress={this.toggleExpand}>
        <List.Section title='Person Detected'>
          <List.Item title='Record' right={() => this.renderSwitch('recordPersonDetect')} />
          <List.Item title='Notify' right={() => this.renderSwitch('notifyPersonDetect')} />
        </List.Section>

        <List.Section title='Animal Detected'>
          <List.Item title='Record' right={() => this.renderSwitch('recordAnimalDetect')} />
          <List.Item title='Notify' right={() => this.renderSwitch('notifyAnimalDetect')} />
        </List.Section>

        <List.Section title='Vehicle Detected'>
          <List.Item title='Record' right={() => this.renderSwitch('recordVehicleDetect')} />
          <List.Item title='Notify' right={() => this.renderSwitch('notifyVehicleDetect')} />
        </List.Section>

        <List.Section title='Recording settings'>
          <List.Item title='Record Duration (seconds)' 
                right={() => this.renderPicker('recordingDuration', [5, 15, 30, 60])}/>
          <List.Item title='Wait Duration (mins)'  
                right={() => this.renderPicker('waitPeriodAfterDetection', [1, 5, 10, 15, 30])}/>
        </List.Section>

        <List.Section title='Test Mode'>
          <List.Item title='Enable Test Mode' right={() => this.renderSwitch('testModeEnabled')} />
        </List.Section>

      </List.Accordion>
    );
  }

  renderSwitch(name) {
    const { props } = this;
    return (
      <Switch
        value={props.cameraConfig[name]}
        onValueChange={value => props.onConfigChange(name, value)}
      />
    );
  }

  renderPicker(name, options){
    const { props } = this;
    return(
      <Picker
        selectedValue={props.cameraConfig[name]}
        style={{height: 50, width: 100}}
        onValueChange={value => props.onConfigChange(name, value)} >
        {options.map((option, index) => (
            <Picker.Item key={name+option+index} label={option + ''} value={option} />
        ))}
      </Picker>
    )
  }
}
