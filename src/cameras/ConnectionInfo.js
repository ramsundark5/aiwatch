import React, { Component } from 'react';
import { ActivityIndicator, Button, List, ToggleButton } from 'react-native-paper';
import ConfigInput from './ConfigInput';
import Theme from '../common/Theme';
import RNSmartCam from '../native/RNSmartCam';
import { Image, View, Text } from 'react-native';
export default class ConnectionInfo extends Component {
  
  state = {
    expanded: true,
    base64Image: null
  };

  toggleExpand = () => {
    this.setState({
      expanded: !this.state.expanded
    });
  };

  async testConnection(){
    const { cameraConfig } = this.props;
    if(cameraConfig && cameraConfig.videoUrl){
      try{
        let image = await RNSmartCam.testCameraConnection(cameraConfig.videoUrl);
        this.setState({base64Image: image});
      }catch(err){
        console.log('error loading test image '+err);
      }
    }
  }

  render() {
    const { props, state } = this;
    return (
      <List.Accordion title="Connection Info" expanded={state.expanded} onPress={this.toggleExpand}>
        <ConfigInput {...props} label="Video URL" name="videoUrl" />
        <Button mode='outlined' color={Theme.primary} onPress={() => this.testConnection()}>
          Test Connection
        </Button>
        {this.renderTestImage()}
        <ConfigInput {...props} label="Username" name="username" />

        <ConfigInput {...props} label="Password" name="password" />
      </List.Accordion>
    );
  }

  renderTestImage(){
    const { base64Image } = this.state;
    const imageUri = 'data:image/png;base64,'+base64Image;
    if(!base64Image){
      return(
        <ActivityIndicator animating={true} size={36} />
      )
    }
    return(
      <View>
        <Text>Does the image look correct?</Text>
         <ToggleButton.Group onValueChange={value => this.setState({ base64Image: null })}>
            <ToggleButton icon="format-align-left" value="Yes" />
            <ToggleButton icon="format-align-right" value="No" />
          </ToggleButton.Group>
          <Image source={{uri: imageUri}}/>
      </View>
      
    )
  }
}
