import React from "react";
import { StyleSheet, Text, View } from "react-native";
import VideoPlayer from '../videoplayer';

class RTSPVideoPlayer extends React.PureComponent{

  constructor(props){
    super(props);
    this.videoPlayer = {};
  }

  onPlay = async(playing) => {
    const { enableHLSLiveView } = this.props;
    if(playing && enableHLSLiveView){
        await enableHLSLiveView(false);
    }
  };

  onErrorRetry =  (err) => {
    const { enableHLSLiveView } = this.props;
    if(err & err.error && err.error.errorException){
      if(err.error.errorException.startsWith('java.io.FileNotFoundException')){
        console.log('error loading video '+ err.error.errorException);
        if(enableHLSLiveView){
            enableHLSLiveView(false);
        }
      }
    }
  };

  onFullScreenEvent(status) {
    const { onFullScreen, cameraConfig } = this.props;
    if(onFullScreen){
      let cameraId = cameraConfig.id;
      if(!status){
        cameraId = null;
      }
      onFullScreen(status, cameraId);
    }
  }

  render(){
    const { hideTime, hideScrubber, title, url } = this.props;
    return(
        <VideoPlayer 
          url={url} 
          onPlay={this.onPlay}
          error={false}
          onError={this.onErrorRetry}
          title={title}
          hideFullScreenControl={false}
          hideScrubber={hideScrubber}
          hideTime={hideTime}
          onFullScreen={status => this.onFullScreenEvent(status)}
          rotateToFullScreen={true}
          lockPortraitOnFsExit={true}
          scrollBounce={true}/>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  toolbar: {
    marginTop: 30,
    backgroundColor: "white",
    padding: 10,
    borderRadius: 5,
  },
  mediaPlayer: {
    height: 300,
    width: '100%',
    backgroundColor: "black",
  },
});

export default RTSPVideoPlayer;