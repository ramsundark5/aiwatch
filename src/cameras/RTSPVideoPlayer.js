import React, { Component } from "react";
import { StyleSheet, Text, View } from "react-native";
import Video from 'react-native-video';
import VideoPlayer from 'react-native-video-controls';

export default class RTSPVideoPlayer extends Component {

  render2(){
    return (
      <VideoPlayer
      style={{flex: 1, height: '100%'}}
      resizeMode={'cover'}
        source={{uri: this.props.url}}
        navigator={this.props.navigator}
      />
    )
  }

  render(){
      return(
          <Video source={{uri: this.props.url}}   // Can be a URL or a local file.
              ref={(ref) => {
                  this.player = ref
              }}
              useTextureView={true}
              paused={true}      
              controls={true}                                // Store reference
              onError={this.videoError}               // Callback when video cannot be loaded
              style={{width: '100%', height: 300 }} />
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
    position: "absolute",
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
    backgroundColor: "black",
  },
});