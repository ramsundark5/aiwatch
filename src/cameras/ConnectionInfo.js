import React, { Component } from 'react';
import { ActivityIndicator, Button, List } from 'react-native-paper';
import ConfigInput from './ConfigInput';
import Theme from '../common/Theme';
import RNSmartCam from '../native/RNSmartCam';
import { Alert, Image, Linking, View, Text } from 'react-native';
import Logger from '../common/Logger';
import testID from '../common/testID';

const initialMessage = 'An image from your camera will appear here on successful test.';
export default class ConnectionInfo extends Component {
  
  state = {
    expanded: true,
    base64Image: undefined,
    isLoading: false,
    testImageMessage: initialMessage
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
      let image = await RNSmartCam.testCameraConnection(cameraConfig);
      this.setState({ base64Image: image, testImageMessage: initialMessage });
    }catch(err){
      Logger.log('error loading test image');
      let testImageMessage = 'Failed to retrieve image from camera. Is the url correct?';
      this.setState({testImageMessage: testImageMessage});
      //Logger.error(err);
    }finally{
      this.setState({ isLoading: false });
    }
  }

  onPressURLFinderButton(){
    Alert.alert(
      'Open camera url finder in new browser window?',
      'This will open ispyconnect.com to help find your camera url. '+
      ' Press OK if you want to continue ',
      [
        {
          text: 'Cancel',
          onPress: () => console.log('Cancel Pressed'),
          style: 'cancel',
        },
        {text: 'OK', onPress: () => Linking.openURL('https://www.ispyconnect.com/sources.aspx')},
      ]
    );
}

  render() {
    const { props, state } = this;
    return (
      <List.Accordion title="Connection Info" expanded={state.expanded} onPress={this.toggleExpand}>
        <ConfigInput {...props} label="Video URL" name="videoUrl"  {...testID('VIDEO_URL')}/>
        <Button color={Theme.primary} onPress={() => this.onPressURLFinderButton()} uppercase={false}>
            Need help finding your video url?
        </Button>
        <ConfigInput {...props} label="Username" name="username"  {...testID('USERNAME')}/>
        <ConfigInput {...props} label="Password" name="password" secureTextEntry={true}  {...testID('PASSWORD')}/>
        <Button color={Theme.primary} onPress={() => this.testConnection()}  {...testID('TEST_CONNECTION')}>
          Test Connection
        </Button>
        {this.renderTestImage()}
      </List.Accordion>
    );
  }

  renderTestImage(){
    const { isLoading, base64Image, testImageMessage } = this.state;
    const imageUri = 'data:image/png;base64,'+base64Image;
    if(isLoading){
      return(
        <View style={{height: 150}}>
          <ActivityIndicator animating={true} size={36} style={{flex: 1, justifyContent: 'center'}}/>
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
    return(
      <View>
        <Text>{testImageMessage}</Text>
      </View>
    )
  }
}
