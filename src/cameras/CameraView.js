import React, { Component } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import RTSPVideoPlayer from './RTSPVideoPlayer';
import { FAB, Portal, Provider } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import { loadCameras, deleteCamera, updateMonitoringStatus, updateStatus } from '../store/CamerasStore';
import { connect } from 'react-redux';
import CameraControl from './CameraControl';
import Logger from '../common/Logger';
import MonitoringStatus from './MonitoringStatus';
import AiwatchUtil from '../common/AiwatchUtil';
class CameraView extends Component {

  static navigationOptions = {
    header: null,
  };

  state = {
    isFull: false,
    open: false
  }

  componentDidMount(){
    this.loadAllCameras();
  }

  async loadAllCameras(){
    const { loadCameras } = this.props;
    try{
      let cameras = await RNSmartCam.getAllCameras();
      loadCameras(cameras);
    }catch(err){
      Logger.error(err);
    }
  }

  onAddCamera(){
    const uuid = AiwatchUtil.uuidv4();
    this.props.navigation.navigate('EditCamera', {
      cameraConfig: {
        uuid: uuid
      }
    });
  }

  onScanCamera(){
    this.props.navigation.navigate('ScanCamera');
  }

  onPlayVideoFullScreen(videoUrl){
    this.props.navigation.navigate('FullScreenVideo', {
      videoUrl: videoUrl
    });
  }

  onCloseFullScreen(){
    this.setState({ isFull: false});
  }

  render() {
    const { isFull } = this.state;
    const { cameras } = this.props;
    return (
      <View style={[styles.container, { marginTop: isFull ? 0 : 20 }]}>
        <MonitoringStatus loadAllCameras={() => this.loadAllCameras()} {...this.props}/>
        <ScrollView
          ref={ref => (this.scrollRef = ref)}
          style={{ flex: 1 }}
          scrollEnabled={isFull ? false : true}
          contentContainerStyle={{
            flex: isFull ? 1 : 0,
          }}>
          {cameras.map((cameraConfig) => (
            this.renderVideoPlayer(cameraConfig)
          ))}
        </ScrollView>
        {this.renderAddCameraButton()}
      </View>
    );
  }

  renderVideoPlayer(cameraConfig){
    if(!cameraConfig.videoUrlWithAuth){
      //return null;
      //cameraConfig.videoUrl = 'rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov';
    }
    return(
      <View style={[styles.container]} key={cameraConfig.id}>
        <Text>{cameraConfig.name}</Text>
        <RTSPVideoPlayer
              style={{width:'100%'}}
              isLive={true}
              showFullScreen={true}
              key={cameraConfig.id}
              url={cameraConfig.videoUrlWithAuth}
              onFullPress={(videoUrl) => this.onPlayVideoFullScreen(videoUrl)}/>
          <CameraControl {...this.props} cameraConfig={cameraConfig}/>
      </View>
    )
  }

  renderAddCameraButton(){
    if(this.state.isFull){
      return null;
    }
    return(
      <Provider>
         <Portal>
           <FAB.Group
             open={this.state.open}
             icon={this.state.open ? 'today' : 'add'}
             actions={[
               { icon: 'star', label: 'Manual Add', onPress: () => this.onAddCamera()},
               { icon: 'email', label: 'Scan', onPress: () => this.onScanCamera() },
             ]}
             onStateChange={({ open }) => this.setState({ open })}
           />
         </Portal>
      </Provider>
    )
  }
}

const mapStateToProps = state => ({
  cameras: state.cameras.cameras,
  isMonitoringOn: state.cameras.isMonitoringOn
});

export default connect(
  mapStateToProps,
  { loadCameras, deleteCamera, updateMonitoringStatus, updateStatus }
)(CameraView);

const styles = StyleSheet.create({
  container: {
    flex: 1,
    width: '100%',
    padding: 10
  },
  footer: {
    width: '100%',
    paddingTop: 20,
  }
});
