import React, { Component } from 'react';
import { View, StyleSheet, ToastAndroid } from 'react-native';
import CameraInfo from './CameraInfo';
import ConnectionInfo from './ConnectionInfo';
import MonitorInfo from './MonitorInfo';
import { Button } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import Theme from '../common/Theme';
import { editCamera } from '../store/CamerasStore';
import { connect } from 'react-redux';
import Logger from '../common/Logger';
import testID from '../common/testID';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
class EditCamera extends Component {

  static navigationOptions = {
    headerTitle: 'Manage Camera',
    headerTintColor: Theme.primary,
    headerTitleStyle: {
      fontSize: 16,
      fontWeight: 'normal',
    }
  };

  constructor(props) {
    super(props);
    this.state = {
      cameraConfig: {},
      loading: false
    };
  }

  componentDidMount(){
    const newCameraConfig = this.props.navigation.getParam('cameraConfig', {});
    this.setState({
      cameraConfig: newCameraConfig
    });
  }

  onConfigChange(key, value) {
    const { cameraConfig } = this.state;
    this.setState({
      cameraConfig: Object.assign({}, cameraConfig, { [key]: value })
    });
  }

  async onSaveCameraConfig() {
    const { cameraConfig } = this.state;
    const { editCamera } = this.props;
    try{
      this.setState({loading: true});
      let updatedCameraConfig = await RNSmartCam.putCamera(cameraConfig);
      this.setState({
        cameraConfig: updatedCameraConfig
      });
      console.log('camera config updated from UI');
      editCamera(updatedCameraConfig);
      this.props.navigation.goBack();
      ToastAndroid.showWithGravity("Changes saved successfully", ToastAndroid.SHORT, ToastAndroid.CENTER);
    }catch(err){
      ToastAndroid.showWithGravity("Error saving you changes. Try again", ToastAndroid.SHORT, ToastAndroid.CENTER);
      Logger.error(err);
    }finally{
      this.setState({loading: false});
    }
  }

  render() {
    const { cameraConfig, loading } = this.state;
    return (
      <View style={styles.container}>
        <KeyboardAwareScrollView 
          {...testID('EDIT_CAMERA_SCROLL_VIEW')}
          style={{ flex: 1 }}
          scrollEnabled={true}
          contentContainerStyle={{
            flex: 0,
          }}>
          <CameraInfo
            cameraConfig={cameraConfig}
            onConfigChange={(key, value) => this.onConfigChange(key, value)}
          />
          <ConnectionInfo
            cameraConfig={cameraConfig}
            onConfigChange={(key, value) => this.onConfigChange(key, value)}
          />
          <MonitorInfo
            cameraConfig={cameraConfig}
            onConfigChange={(key, value) => this.onConfigChange(key, value)}
          />
        </KeyboardAwareScrollView>
        <View style={styles.footer}>
          <Button mode='contained' color={Theme.primary} 
              loading={loading}
              onPress={() => this.onSaveCameraConfig()}
              {...testID('SAVE_CAMERA')}>
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
)(EditCamera);

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 10,
  },
  footer: {
    width: '100%',
    paddingTop: 20
  }
});