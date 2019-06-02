import React, { Component } from 'react';
import { ActivityIndicator, Button, List } from 'react-native-paper';
import ConfigInput from './ConfigInput';
import Theme from '../common/Theme';
import RNSmartCam from '../native/RNSmartCam';
import { Image, View, Text } from 'react-native';
import Logger from '../common/Logger';

export default class ConnectionInfo extends Component {
  
  state = {
    expanded: true,
    base64Image: undefined,
    isLoading: false
  };

  toggleExpand = () => {
    this.setState({
      expanded: !this.state.expanded
    });
  };

  testConnection(){
    const { cameraConfig } = this.props;
    if(cameraConfig && cameraConfig.videoUrl){
      this.setState({ isLoading: true });
      requestAnimationFrame(() => {
        this.loadBase64Image(cameraConfig);
      });
    }
  }

  async loadBase64Image(cameraConfig){
    try{
      let image = await RNSmartCam.testCameraConnection(cameraConfig.videoUrl);
      this.setState({ base64Image: image });
    }catch(err){
      Logger.log('error loading test image');
      Logger.error(err);
    }finally{
      this.setState({ isLoading: false });
    }
  }

  render() {
    const { props, state } = this;
    return (
      <List.Accordion title="Connection Info" expanded={state.expanded} onPress={this.toggleExpand}>
        <ConfigInput {...props} label="Video URL" name="videoUrl" />
        <ConfigInput {...props} label="Username" name="username" />
        <ConfigInput {...props} label="Password" name="password" />
        <Button mode='outlined' color={Theme.primary} onPress={() => this.testConnection()}>
          Test Connection
        </Button>
        {this.renderTestImage()}
      </List.Accordion>
    );
  }

  renderTestImage(){
    const { isLoading, base64Image } = this.state;
    const imageUri = 'data:image/png;base64,'+base64Image;
    if(isLoading){
      return(
        <View style={{height: 150}}>
          <ActivityIndicator animating={true} size={36} style={{flex: 1, justifyContent: 'center'}}/>
        </View>
      )
    }
    if(base64Image === 'undefined'){
      return(
        <View>
          <Text>Failed to retrive image from camera. Is the url correct?</Text>
        </View>
      )
    }
    if(base64Image){
      return(
        <View>
          <Text>Does the image look correct?</Text>
            <Image source={{uri: imageUri}} style={{width: '100%', height: 256}}/>
        </View>
      )
    }
    
  }
}
