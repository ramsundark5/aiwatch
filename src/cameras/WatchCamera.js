import React, { Component } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import RTSPVideoPlayer from './RTSPVideoPlayer';
import RNSmartCam from '../native/RNSmartCam';
import CameraControl from './CameraControl';
import { moderateScale, verticalScale } from 'react-native-size-matters';
import AiwatchUtl from '../common/AiwatchUtil';
import ErrorBoundary from '../common/ErrorBoundary';

class WatchCamera extends Component {

    constructor(props) {
        super(props);
    }

    state = {
        baseHLSPath: null,
    }

    async enableHLSLiveView(errored, cameraConfig){
        try{
            if(cameraConfig.videoUrl && cameraConfig.videoUrl.startsWith('rtsp')){
                if(errored){
                  this.updateHLSLiveViewStatus(cameraConfig, false);
                }else{
                  let isCameraRunning = await RNSmartCam.getCameraMonitoringStatus(cameraConfig.id);
                  this.updateHLSLiveViewStatus(cameraConfig, true);
                  await RNSmartCam.putCamera(camerConfigUpdate);
                  if(!isCameraRunning){
                      //sleep for 5 seconds so ffmpeg can init rtsp
                      //await AiwatchUtl.sleep(5000);
                  }
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
        const cameraConfig = this.props.cameraConfig;
        if(!cameraConfig){
          return null;
        }
        let videUrlForView = cameraConfig.videoUrl;
        if(cameraConfig.videoUrl && cameraConfig.videoUrl.startsWith('rtsp') && cameraConfig.rtspUrl && cameraConfig.rtspUrl.length > 1){
          videUrlForView = "file://" + cameraConfig.rtspUrl;
        }
        return(
          <View key={cameraConfig.id}>
            <RTSPVideoPlayer
                  {...this.props}
                  enableHLSLiveView={(errored) => this.enableHLSLiveView(errored, cameraConfig)}
                  key={cameraConfig.id}
                  url={videUrlForView}
                  title={cameraConfig.name}
                  hideScrubber={true}
                  hideTime={true}/>
              <CameraControl {...this.props} cameraConfig={cameraConfig}/>
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