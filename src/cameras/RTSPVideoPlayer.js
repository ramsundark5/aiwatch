import React from "react";
import { StyleSheet, Text, View } from "react-native";
import VideoPlayer from '../videoplayer';
import { VlCPlayerView } from 'react-native-vlc-media-player';

class RTSPVideoPlayer extends React.PureComponent{

  constructor(props){
    super(props);
  }

  state = {
    fullScreen: false
  }

  onError = (err) => {
    console.log('error loading video ' + JSON.stringify(err));
  };

  onFullScreenEvent(status) {
    const { onFullScreen, cameraConfig } = this.props;
    this.setState({fullScreen: status});
    if(onFullScreen){
      let cameraId = cameraConfig.id;
      if(!status){
        cameraId = null;
      }
      onFullScreen(status, cameraId);
    }
  }

  render(){
    return(
        <VideoPlayer 
          {...this.props}
          ref={(ref) => { this.videoRef = ref }}
          onError={this.onError}
          showBack={true}
          autoAspectRatio={true}
          hideFullScreenControl={false}
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
  fullScreen: {
    ...StyleSheet.absoluteFillObject
  },
});

export default RTSPVideoPlayer;