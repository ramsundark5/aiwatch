import React from "react";
import { StyleSheet, Text, View } from "react-native";
import Video from "react-native-video";
import MediaControls, { PLAYER_STATES } from '../videoplayer';

class RTSPVideoPlayer2 extends React.PureComponent{

  constructor(props){
    super(props);
    this.videoPlayer = {};
  }

  state = {
    currentTime: 0,
    duration: 0,
    isFullScreen: false,
    isLoading: false,
    paused: true,
    resizeMode: 'contain',
    playerState: PLAYER_STATES.PAUSED
  }

  componentDidCatch(error, info) {
    // Display fallback UI
    // You can also log the error to an error reporting service
    console.log(error, info);
    //this.props.navigateTo('CameraView');
  }

  onSeek = seek => {
    //this.videoPlayer?.seek(seek);
  };

  onPaused = async(playerState) => {
    const { enableHLSLiveView } = this.props;
    if(playerState == PLAYER_STATES.PLAYING && enableHLSLiveView){
        this.setState({ isLoading: true });
        //invoke the callback
        await enableHLSLiveView(false);
        this.setState({ isLoading: false });
    }
    let paused = true;
    if(playerState == PLAYER_STATES.PLAYING){
      paused = false;
    }
    this.setState({ paused: paused, playerState: playerState });
  };

  onPlaybackRateChange = (playbackRate) => {
    if(playbackRate === 0){
      this.setState({ paused: true, playerState: PLAYER_STATES.PAUSED });
    }else{
      this.setState({ paused: false, playerState: PLAYER_STATES.PLAYING });
    }
  };

  onReplay = () => {
    this.setState({ playerState: PLAYER_STATES.PLAYING });
    //this.videoPlayer?.seek(0);
  };

  onProgress = data => {
    // Video Player will continue progress even if the video already ended
    if (!isLoading && playerState !== PLAYER_STATES.ENDED) {
      this.setState({ currentTime: data.currentTime });
    }
  };

  onLoad = data => {
    this.setState({ currentTime: data.currentTime, isLoading: false });
  };

  onError = (err) => {
    this.setState({ playerState: PLAYER_STATES.PAUSED, isLoading: false });
  };
  
  onFullScreen = (isFullScreen) => {
    this.setState({ isFullScreen: isFullScreen });
  }

  onLoadStart = () => {
    this.setState({ isLoading: true });
  }

  onEnd = () => {
    if(this.props.showSlider){
      this.setState({ playerState: PLAYER_STATES.ENDED });
    }else{
      this.setState({ playerState: PLAYER_STATES.PAUSED });
    }
  }

  onSeeking = currentTime => {
    this.setState({ currentTime: currentTime });
  }

  render(){
    const { onEnd, onError, onLoad, onLoadStart, onProgress, onFullScreen, onPaused, onReplay, onSeek, onSeeking, onPlaybackRateChange} = this;
    const { paused, videoUrl, isFullScreen, duration, isLoading, playerState, currentTime } = this.state;
    const { showDuration, showSlider, url } = this.props;
    console.log('video url in render '+ url);
    if(!url){
      console.log('video url is null');
      return null;
    }
    return (
      <View style={styles.container}>
        <Video
          paused={paused}
          ref={ref => (this.videoPlayer = ref)}
          resizeMode="contain"
          source={{uri: url}}
          useTextureView={true}
          fullscreen={isFullScreen}
          onPlaybackRateChange={onPlaybackRateChange}
          style={styles.mediaPlayer}
        />
        <MediaControls
          showSlider={showSlider}
          showDuration={showDuration}
          isFullScreen={isFullScreen}
          duration={duration}
          isLoading={isLoading}
          mainColor="red"
          onFullScreen={onFullScreen}
          onPaused={onPaused}
          onReplay={onReplay}
          onSeek={onSeek}
          onSeeking={onSeeking}
          playerState={playerState}
          progress={currentTime}>
        <MediaControls.Toolbar>
            <View style={styles.toolbar}>
              <Text>I'm a custom toolbar </Text>
            </View>
          </MediaControls.Toolbar>
        </MediaControls>
      </View>
    );
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

export default RTSPVideoPlayer2;