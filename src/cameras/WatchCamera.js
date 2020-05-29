import React, { Component } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import RTSPVideoPlayer from './RTSPVideoPlayer';
import RNSmartCam from '../native/RNSmartCam';
import CameraControl from './CameraControl';
import Logger from '../common/Logger';
import { moderateScale, verticalScale } from 'react-native-size-matters';
import AiwatchUtl from '../common/AiwatchUtil';
class WatchCamera extends Component {

    constructor(props) {
        super(props);
    }

    state = {
        baseHLSPath: null,
    }

    componentDidMount(){
        this.init();
    }

    async init(){
        try{
          let basePath = await RNSmartCam.geBaseHLSPath();
          this.setState({baseHLSPath: basePath});
        }catch(err){
          Logger.error(err);
        }
    }

    onPlayVideoFullScreen(videoUrl){
        this.props.navigation.navigate('FullScreenVideo', {
            videoUrl: videoUrl
        });
    }

    onCloseFullScreen(){
        this.setState({ isFull: false});
    }

    async enableHLSLiveView(paused, cameraConfig){
        try{
            let camerConfigUpdate = Object.assign({}, cameraConfig);
            camerConfigUpdate.liveHLSViewEnabled = false;
            if(cameraConfig.videoUrl && cameraConfig.videoUrl.startsWith('rtsp')){
                let isCameraRunning = await RNSmartCam.getCameraMonitoringStatus(cameraConfig.id);
                camerConfigUpdate.liveHLSViewEnabled = true;
                await RNSmartCam.putCamera(camerConfigUpdate);
                if(!isCameraRunning){
                    //sleep for 5 seconds so ffmpeg can init rtsp
                    await AiwatchUtl.sleep(5000);
                }
            }
        }catch(err){
            Logger.error(err);
        }
    }
  
    render(){
        const cameraConfig = this.props.cameraConfig;
        const baseHLSPath = this.state.baseHLSPath;
        if(!cameraConfig){
          return null;
        }
        let playerHeight = verticalScale(211.5);
        let playerMaxWidth = moderateScale(400);
        let rtspUrl = '';
        if(cameraConfig.videoUrl && cameraConfig.videoUrl.startsWith('rtsp')){
          let rtspUrlOld = baseHLSPath + "/camera" + cameraConfig.id + ".m3u8";
          console.log('old rtsp video url for view '+rtspUrlOld);
          rtspUrl = cameraConfig.rtspUrl;
        }
        let videUrlForView = cameraConfig.videoUrl;
        if(cameraConfig.videoUrl && cameraConfig.videoUrl.startsWith('rtsp')){
          videUrlForView = "file://" + baseHLSPath + "/camera" + cameraConfig.id + ".m3u8";
        }
        console.log('rtsp video url for view '+rtspUrl);
        console.log('player height '+playerHeight);
        return(
          <View style={[styles.container, {alignSelf: 'center', maxWidth: playerMaxWidth}]} key={cameraConfig.id}>
            <Text>{cameraConfig.name}</Text>
            <RTSPVideoPlayer
                  style={{width:'100%', height: playerHeight}}
                  enableHLSLiveView={(paused) => this.enableHLSLiveView(paused, cameraConfig)}
                  key={cameraConfig.id}
                  url={videUrlForView}
                  rtspUrl={rtspUrl}
                  showSlider={false}
                  showDuration={false}
                  onFullPress={(videoUrl) => this.onPlayVideoFullScreen(videoUrl)}/>
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