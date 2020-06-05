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

  render(){
    const { showDuration, showSlider, url } = this.props;
    return(
      <View style={styles.container}>
        <VideoPlayer 
          url={url} 
          onPlay={this.onPlay}/>
      </View>
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