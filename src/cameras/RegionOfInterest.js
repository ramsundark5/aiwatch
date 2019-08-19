import CustomCrop from '../common/CustomCrop';
import React, { Component } from 'react';
import { ActivityIndicator, Button } from 'react-native-paper';
import { View, Image, Text } from 'react-native';
import Logger from '../common/Logger';
import RNSmartCam from '../native/RNSmartCam';

export default class RegionOfInterest extends Component{

    static navigationOptions = {
      headerTitle: 'Region of Interest',
      headerTintColor: Theme.primary,
      headerTitleStyle: {
        fontSize: 16,
        fontWeight: 'normal',
      }
    };

    state = {
      base64Image: undefined,
      isLoading: false,
      testImageMessage: ''
    };

    componentWillMount() {
      this.init();
    }
    
    init(){
      const cameraConfig = this.props.navigation.getParam('cameraConfig', {});
      if(cameraConfig && cameraConfig.videoUrl){
        this.setState({ isLoading: true });
        requestAnimationFrame(() => {
          this.loadBase64Image(cameraConfig);
        });
      }
    }
  
    async loadBase64Image(cameraConfig){
      try{
        let base64Image = await RNSmartCam.testCameraConnection(cameraConfig);
        const imageUri = 'data:image/png;base64,'+base64Image;
        this.setState({
          imageWidth: 300,
          imageHeight: 300,
          base64Image: imageUri,
          testImageMessage: null
        });
      }catch(err){
        Logger.log('error loading test image');
        let testImageMessage = 'Failed to retrieve image from camera. Is the url correct?';
        this.setState({testImageMessage: testImageMessage});
      }finally{
        this.setState({ isLoading: false });
      }
    }

    updateImage(image, newCoordinates) {
        this.setState({
          image,
          rectangleCoordinates: newCoordinates
        });
    }
    
    onSaveROI(){
      const coordinates = this.customCrop.selectROI()
      console.log(coordinates);
    }
    
    render() {
      const { isLoading, base64Image, testImageMessage } = this.state;
      if(isLoading){
        return(
          <View style={{height: 150}}>
            <ActivityIndicator animating={true} size={36} style={{flex: 1, justifyContent: 'center'}}/>
          </View>
        )
      }
      if(base64Image){
        return this.renderROIOverlay();
      }
      return(
        <View>
          <Text>{testImageMessage}</Text>
        </View>
      )
    }

    renderROIOverlay(){
      return (
        <View style={{flex: 1}}>
          <CustomCrop
            updateImage={this.updateImage.bind(this)}
            rectangleCoordinates={this.state.rectangleCoordinates}
            initialImage={this.state.base64Image}
            height={300}
            width={300}
            ref={ref => (this.customCrop = ref)}
            overlayColor="rgba(18,190,210, 1)"
            overlayStrokeColor="rgba(20,190,210, 1)"
            handlerColor="rgba(20,150,160, 1)"
            enablePanStrict={false}
          />
          <View style={{width: '100%', paddingTop: 20}}>
            <Button mode='outlined' color={Theme.primary} onPress={() => this.onSaveROI()}>
              Save
            </Button>
          </View>
        </View>
      );
    }
}