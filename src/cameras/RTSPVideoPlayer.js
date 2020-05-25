import React, { Component } from 'react';
import { VlcSimplePlayer } from 'react-native-yz-vlcplayer';
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

  render2() {
    return (
      <VlcSimplePlayer
        {...this.props}
        showAd={false}
        showBack={false}
        initType={2}
        hwDecoderEnabled={1}
        hwDecoderForced={1}
        initOptions={[
          "--no-audio",
          "--rtsp-tcp",
          "--network-caching=" + 150,
          "--rtsp-caching=" + 150,
          "--no-stats",
          "--tcp-caching=" + 150,
          "--realrtsp-caching=" + 150
        ]}
        endingViewText={{
          endingText: "End",
          reloadBtnText: "Reload",
          nextBtnText: "Next"
        }}
        errorViewText={{
          errorText: "Error",
          reloadBtnText: "Reload"
        }}
        vipEndViewText={{
          vipEndText: "End",
          boughtBtnText: "Please buy and watch now to buy"
        }}
      />
    );
  }
}
