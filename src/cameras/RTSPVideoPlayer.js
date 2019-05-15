import React, { Component } from 'react';
import { VlcSimplePlayer } from 'react-native-yz-vlcplayer';

export default class RTSPVideoPlayer extends Component {
  render() {
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
          endingText: "end",
          reloadBtnText: "reload",
          nextBtnText: "next"
        }}
        errorViewText={{
          errorText: "error",
          reloadBtnText: "reload"
        }}
        vipEndViewText={{
          vipEndText: "end",
          boughtBtnText: "Please buy and watch now to buy"
        }}
      />
    );
  }
}
