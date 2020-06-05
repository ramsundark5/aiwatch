import React from "react";
import { StyleSheet, Text, View } from "react-native";
import Video from "react-native-video";
import MediaControls, { PLAYER_STATES } from '../videoplayer';
import VideoPlayer from '../videoplayer2';

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
    this.videoPlayer?.seek(seek);
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

  onPlay = async(playing) => {
    const { enableHLSLiveView } = this.props;
    if(playing && enableHLSLiveView){
        this.setState({ isLoading: true });
        //invoke the callback
        await enableHLSLiveView(false);
        this.setState({ isLoading: false });
    }
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
    this.videoPlayer?.seek(0);
  };

  onProgress = data => {
    const { isLoading, playerState } = this.state;
    // Video Player will continue progress even if the video already ended
    if (!isLoading && !this.props.showDuration && playerState !== PLAYER_STATES.ENDED) {
      this.setState({ currentTime: data.currentTime });
    }
  };

  onLoad = data => {
    //if(data && data.currentTime){
      this.setState({ currentTime: data.currentTime, isLoading: false });
    //}
  };

  onError = (err) => {
    console.log('error playing video '+ err);
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
    const { showDuration, showSlider, url } = this.props;
    return(
      <View style={styles.container}>
        <VideoPlayer url={url} onPlay={this.onPlay}/>
      </View>
    )
  }
  
  render2(){
    const { onEnd, onError, onLoad, onLoadStart, onProgress, onFullScreen, onPaused, onReplay, onSeek, onSeeking, onPlaybackRateChange} = this;
    const { paused, isFullScreen, duration, isLoading, playerState, currentTime } = this.state;
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
          onLoad={onLoad}
          onLoadStart={onLoadStart}
          onError={onError}
          onEnd={onEnd}
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