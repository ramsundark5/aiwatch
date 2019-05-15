import React, { Component } from 'react';
import { View, ScrollView, StyleSheet } from 'react-native';
import CameraInfo from './CameraInfo';
import ConnectionInfo from './ConnectionInfo';
import MonitorInfo from './MonitorInfo';
import { Button } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
export default class ManageCamera extends Component {
  constructor(props) {
    super(props);
    this.state = {
      cameras: []
    };
  }

  componentDidMount(){
    this.loadAllCamera();
  }

  async loadAllCamera(){
    let cameras = await RNSmartCam.getAllCameras();
    this.setState({cameras: cameras});
  }

  render() {
    const { cameraConfig } = this.state;
    return (
      <View style={styles.container}>
        <ScrollView>
          <CameraInfo
            cameraConfig={cameraConfig}
            onConfigChange={(key, value) => this.onConfigChange(key, value)}/>
          <ConnectionInfo
            cameraConfig={cameraConfig}
            onConfigChange={(key, value) => this.onConfigChange(key, value)}/>
          <MonitorInfo
            cameraConfig={cameraConfig}
            onConfigChange={(key, value) => this.onConfigChange(key, value)}/>
        </ScrollView>
        <View style={styles.footer}>
          <Button mode="contained" onPress={() => this.onSaveCameraConfig()}>
            Save
          </Button>
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 10
  },
  footer: {
    height: 100,
    width: '100%',
    paddingTop: 20
  }
});
