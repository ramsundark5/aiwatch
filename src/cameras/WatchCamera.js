import React, { Component } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import RTSPVideoPlayer from './RTSPVideoPlayer';
import RNSmartCam from '../native/RNSmartCam';
import CameraControl from './CameraControl';

class WatchCamera extends Component {

    constructor(props) {
        super(props);
    }

    state = {
        baseHLSPath: null,
        autoplay: false
    }

    async enableHLSLiveView(errored, cameraConfig){
        try{
            if(cameraConfig.videoUrl && cameraConfig.videoUrl.startsWith('rtsp')){
                if(errored){
                  //await this.updateHLSLiveViewStatus(cameraConfig, false);
                }else{
                  //let isCameraRunning = await RNSmartCam.getCameraMonitoringStatus(cameraConfig.id);
                  //await this.updateHLSLiveViewStatus(cameraConfig, true);
                  this.setState({autoplay: true});
                }
            }
        }catch(err){
          console.log('error enabling hls ' + err);
        }
    }
  
    async updateHLSLiveViewStatus(cameraConfig, enable){
      try{
        let camerConfigUpdate = Object.assign({}, cameraConfig);
        camerConfigUpdate.liveHLSViewEnabled = enable;
        await RNSmartCam.putCamera(camerConfigUpdate);
      }catch(err){
        console.log('error updating hls status ' + err);
      }
    }

    render(){
        const { cameraConfig, fullscreen } = this.props;
        if(!cameraConfig){
          return null;
        }
        let videUrlForView = cameraConfig.videoUrl;
        const monitoring = !cameraConfig.disconnected && cameraConfig.monitoringEnabled;
        return(
          <View key={cameraConfig.id}>
            <RTSPVideoPlayer
                  {...this.props}
                  enableHLSLiveView={(errored) => this.enableHLSLiveView(errored, cameraConfig)}
                  key={cameraConfig.id}
                  url={videUrlForView}
                  monitoring={monitoring}
                  autoplay={this.state.autoplay}
                  title={cameraConfig.name}
                  hideScrubber={true}
                  logo=''
                  hideTime={true}/>
              { !fullscreen && 
                <CameraControl {...this.props} cameraConfig={cameraConfig}/>
              }
          </View>
        )
      }
}

const styles = StyleSheet.create({
    container: {
      flex: 1,
      width: '100%',
      padding: 10,
    }
  });

  
export default WatchCamera;