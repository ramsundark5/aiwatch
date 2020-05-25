import React, { Component } from 'react';
import Video from 'react-native-video';

export default class RTSPVideoPlayer extends Component {

  videoError = (err) => {
      console.log('error playing video ' + err);
  };

  render(){
      return(
          <Video source={{uri: this.props.url}}   // Can be a URL or a local file.
              ref={(ref) => {
                  this.player = ref
              }}      
              controls={true}                                // Store reference
              onError={this.videoError}               // Callback when video cannot be loaded
              style={{width: 400, height: 300 }} />
      )
  }
}
