import React from "react";
import { StyleSheet, Text, View } from "react-native";
import Video from "react-native-video";
import MediaControls, { PLAYER_STATES } from '../videoplayer';

class RTSPVideoPlayer2 extends React.Component{

  constructor(props){
    super(props);
    this.videoPlayer = {};
    this.videoPlayer.current = null;
  }

  state = {
    currentTime: 0,
    duration: 0,
    isFullScreen: false,
    isLoading: false,
    paused: true,
    resizeMode: 'contain',
    videoUrl: null,
    playerState: PLAYER_STATES.PAUSED
  }

  componentDidMount(){
    this.setState({videoUrl: this.props.rtspUrl});
  }

  onSeek = seek => {
    this.videoPlayer?.current.seek(seek);
  };

  onPaused = async(playerState) => {
    const { enableHLSLiveView } = this.props;
    if(playerState == PLAYER_STATES.PLAYING && enableHLSLiveView){
        this.setState({ isLoading: true });
        //invoke the callback
        await enableHLSLiveView(false);
        this.setState({ videoUrl: this.props.rtspUrl, isLoading: false });
    }
    this.setState({ paused: !this.state.paused, playerState: playerState });
  };

  onReplay = () => {
    this.setState({ playerState: PLAYER_STATES.PLAYING });
    this.videoPlayer?.seek(0);
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
    const { onEnd, onError, onLoad, onLoadStart, onProgress, onFullScreen, onPaused, onReplay, onSeek, onSeeking} = this;
    const { paused, videoUrl, isFullScreen, duration, isLoading, playerState, currentTime } = this.state;
    const { showDuration, showSlider } = this.props;
    console.log('rtsp url in render '+ videoUrl);
    return (
      <View style={styles.container}>
        <Video
          onEnd={onEnd}
          onError={onError}
          onLoad={onLoad}
          onLoadStart={onLoadStart}
          onProgress={onProgress}
          paused={paused}
          ref={ref => (this.videoPlayer.current = ref)}
          resizeMode="contain"
          source={{uri: videoUrl}}
          useTextureView={true}
          fullscreen={isFullScreen}
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
          progress={currentTime}
        >
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