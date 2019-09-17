import React, { Component } from 'react';
import { Alert, Picker } from 'react-native';
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

  onEnableCvr(value){
    const { onConfigChange } = this.props;
    if(!value){
      onConfigChange('cvrEnabled', value);
      return;
    }

    let warningMessage = 'Ensure you are using the extra/secondary stream in video url. For many cameras this can be done by setting subtype=1 in video url. '
    + 'aiwatch does not compress videos and using primary stream will fill your storage very quickly. ';
    try{
      Alert.alert(
        'Continuous Video Recording',
        warningMessage,
        [
          {
            text: 'Cancel',
            onPress: () => console.log('Cancel Pressed'),
            style: 'cancel',
          },
          {text: 'OK', onPress: () => onConfigChange('cvrEnabled', value)},
        ]
      );
    }catch(err){
      Logger.error(err);
    }
  }

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
                right={() => this.renderPicker('recordingDuration', [10, 15, 30, 45, 60], 15)}/>
          <List.Item title='Wait Duration (mins)'  
                right={() => this.renderPicker('waitPeriodAfterDetection', [1, 5, 10, 15, 30], 5)}/>
        </List.Section>

        <List.Section title='Continuous Recording'>
          <List.Item title='Enable Continuous Recording' right={() => this.renderCvrEnabled()} />
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

  renderCvrEnabled(){
    const { props } = this;
    return (
      <Switch
        value={props.cameraConfig['cvrEnabled']}
        onValueChange={value => this.onEnableCvr(value)}
      />
    );
  }

  renderPicker(name, options, defaultValue){
    const { props } = this;
    const valueFromDB = props.cameraConfig[name];
    const pickerValue = valueFromDB ? valueFromDB : defaultValue;
    return(
      <Picker
        selectedValue={pickerValue}
        style={{height: 50, width: 100}}
        onValueChange={value => props.onConfigChange(name, value)} >
        {options.map((option, index) => (
            <Picker.Item key={name+option+index} label={option + ''} value={option} />
        ))}
      </Picker>
    )
  }
}
