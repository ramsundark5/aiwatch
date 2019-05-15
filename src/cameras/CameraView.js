import React, { Component } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import RTSPVideoPlayer from './RTSPVideoPlayer';
import { Button} from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import Theme from '../common/Theme';
import { loadCameras, deleteCamera } from '../store/CamerasStore';
import { connect } from 'react-redux';
import CameraControl from './CameraControl';
import FullScreenVideoPlayer from '../common/FullScreenVideoPlayer';

class CameraView extends Component {

  state = {
    isFull: false,
    selectedVideoUrl: ''
  }

  componentDidMount(){
    this.loadAllCamera();
  }

  async loadAllCamera(){
    const { loadCameras } = this.props;
    try{
      let cameras = await RNSmartCam.getAllCameras();
      loadCameras(cameras);
    }catch(err){
      console.log(err);
    }
  }

  onAddCamera(){
    this.props.navigation.navigate('EditCamera', {
      cameraConfig: {}
    });
  }

  onPlayVideoFullScreen(videoUrl){
    this.setState({ isFull: true, selectedVideoUrl: videoUrl});
  }

  onCloseFullScreen(){
    this.setState({ isFull: false, selectedVideoUrl: ''});
  }

  render() {
    const { isFull, selectedVideoUrl } = this.state;
    const { cameras } = this.props;
    return (
      <View style={[styles.container, { marginTop: isFull ? 0 : 20 }]}>
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
        <FullScreenVideoPlayer isFull={isFull} videoUrl={selectedVideoUrl} onClose={()=> this.onCloseFullScreen()} />
        {this.renderAddCameraButton()}
      </View>
    );
  }

  renderVideoPlayer(cameraConfig){
    if(!cameraConfig.videoUrl){
      //return null;
      cameraConfig.videoUrl = 'rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov';
    }
    return(
      <View style={[styles.container]} key={cameraConfig.id}>
        <Text>{cameraConfig.name}</Text>
        <RTSPVideoPlayer
              style={{width:'100%'}}
              isLive={true}
              showFullScreen={true}
              key={cameraConfig.id}
              url={cameraConfig.videoUrl}
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
      <View style={styles.footer}>
        <Button mode='outlined' color={Theme.primary} onPress={() => this.onAddCamera()}>
          Add Camera
        </Button>
      </View>
    )
  }
}

const mapStateToProps = state => ({
  cameras: state.cameras.cameras
});

export default connect(
  mapStateToProps,
  { loadCameras, deleteCamera }
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
