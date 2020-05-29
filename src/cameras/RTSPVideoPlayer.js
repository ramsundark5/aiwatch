import React, { useState, useRef } from "react";
import { StyleSheet, Text, View } from "react-native";
import Video from "react-native-video";
import MediaControls, { PLAYER_STATES } from '../videoplayer';

const RTSPVideoPlayer = (props) => {
  const videoPlayer = useRef(null);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [isFullScreen, setIsFullScreen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [paused, setPaused] = useState(true);
  const [resizeMode, setResizeMode] = useState('content');
  const [playerState, setPlayerState] = useState(PLAYER_STATES.PAUSED);

  const onSeek = seek => {
    videoPlayer?.current.seek(seek);
  };

  const onPaused = async(playerState) => {
    if(playerState == PLAYER_STATES.PLAYING && props.enableHLSLiveView){
        setIsLoading(true);
        //invoke the callback
        await props.enableHLSLiveView(false);
        //show spinner for 3 seconds as it takes time to setup rtsp connection
        setTimeout(() => {
          setIsLoading(false);
        }, 1000 * 3);
    }
    setPaused(!paused);
    setPlayerState(playerState);
  };

  const onReplay = () => {
    setPlayerState(PLAYER_STATES.PLAYING);
    videoPlayer?.seek(0);
  };

  const onProgress = data => {
    // Video Player will continue progress even if the video already ended
    if (!isLoading && playerState !== PLAYER_STATES.ENDED) {
      setCurrentTime(data.currentTime);
    }
  };

  const onLoad = data => {
    setDuration(data.duration);
    setIsLoading(false);
  };

  const onError = (err) => {
    setIsLoading(false);
    setPlayerState(PLAYER_STATES.PAUSED);
  };
  
  const onFullScreen = (isFullScreen) => {
    setIsFullScreen(isFullScreen);
  }

  const onLoadStart = () => setIsLoading(true);

  const onEnd = () => {
    if(props.showSlider){
      setPlayerState(PLAYER_STATES.ENDED);
    }else{
      setPlayerState(PLAYER_STATES.PAUSED);
    }
  }

  const onSeeking = currentTime => setCurrentTime(currentTime);

  return (
    <View style={styles.container}>
      <Video
        onEnd={onEnd}
        onError={onError}
        onLoad={onLoad}
        onLoadStart={onLoadStart}
        onProgress={onProgress}
        paused={paused}
        ref={ref => (videoPlayer.current = ref)}
        resizeMode="contain"
        source={{uri: props.url}}
        useTextureView={true}
        fullscreen={isFullScreen}
        style={styles.mediaPlayer}
      />
      <MediaControls
        showSlider={props.showSlider}
        showDuration={props.showDuration}
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
};

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