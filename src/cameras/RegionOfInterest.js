import CustomCrop from '../common/CustomCrop';
import React, { Component } from 'react';
import { ActivityIndicator, Button } from 'react-native-paper';
import { View, ToastAndroid, Text } from 'react-native';
import Logger from '../common/Logger';
import Theme from '../common/Theme';
import { editCamera } from '../store/CamerasStore';
import { connect } from 'react-redux';
import RNSmartCam from '../native/RNSmartCam';

class RegionOfInterest extends Component{

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

    componentDidMount() {
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
    
    async onSaveROI(){
      const { editCamera } = this.props;
      try{
        const coordinates = this.customCrop.selectROI();
        const cameraConfig = this.props.navigation.getParam('cameraConfig', {});
        let cameraConfigWithROI = {...cameraConfig};

        cameraConfigWithROI.topLeftX = coordinates.topLeft.x;
        cameraConfigWithROI.topLeftY = coordinates.topLeft.y;
        cameraConfigWithROI.topRightX = coordinates.topRight.x;
        cameraConfigWithROI.topRightY = coordinates.topRight.y;
        cameraConfigWithROI.bottomLeftX = coordinates.bottomLeft.x;
        cameraConfigWithROI.bottomLeftY = coordinates.bottomLeft.y;
        cameraConfigWithROI.bottomRightX = coordinates.bottomRight.x;
        cameraConfigWithROI.bottomRightY = coordinates.bottomRight.y;

        let updatedCameraConfig = await RNSmartCam.putCamera(cameraConfigWithROI);
        editCamera(updatedCameraConfig);
        //this.props.navigation.goBack();
        ToastAndroid.showWithGravity("Changes saved successfully", ToastAndroid.SHORT, ToastAndroid.CENTER);
      }catch(err){
        Logger.log('error saving roi '+err);
      }
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
      const cameraConfig = this.props.navigation.getParam('cameraConfig', {});
      return (
        <View style={{flex: 1}}>
          <CustomCrop
            updateImage={this.updateImage.bind(this)}
            rectangleCoordinates={cameraConfig}
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

export default connect(
  null,
  { editCamera }
)(RegionOfInterest);