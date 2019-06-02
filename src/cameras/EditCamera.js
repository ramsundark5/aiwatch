import React, { Component } from 'react';
import { View, ScrollView, StyleSheet, ToastAndroid } from 'react-native';
import CameraInfo from './CameraInfo';
import ConnectionInfo from './ConnectionInfo';
import MonitorInfo from './MonitorInfo';
import { Button } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import Theme from '../common/Theme';
import { editCamera } from '../store/CamerasStore';
import { connect } from 'react-redux';
import Logger from '../common/Logger';
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
      cameraConfig: {}
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
      let cameraId = await RNSmartCam.putCamera(cameraConfig);
      const updatedCameraConfig = Object.assign({}, cameraConfig, { id: cameraId });
      this.setState({
        cameraConfig: updatedCameraConfig
      });
      editCamera(updatedCameraConfig);
      this.props.navigation.goBack();
      ToastAndroid.showWithGravity("Changes saved successfully", ToastAndroid.SHORT, ToastAndroid.CENTER);
    }catch(err){
      ToastAndroid.showWithGravity("Error saving you changes. Try again", ToastAndroid.SHORT, ToastAndroid.CENTER);
      Logger.error(err);
    }
  }

  render() {
    const { cameraConfig } = this.state;
    return (
      <View style={styles.container}>
        <ScrollView 
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
        </ScrollView>
        <View style={styles.footer}>
          <Button mode='contained' color={Theme.primary} onPress={() => this.onSaveCameraConfig()}>
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