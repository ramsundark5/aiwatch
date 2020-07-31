import React, { Component } from 'react';
import { View } from 'react-native';
import RTSPVideoPlayer from './RTSPVideoPlayer';
import CameraControl from './CameraControl';

class WatchCamera extends Component {

    constructor(props) {
        super(props);
    }

    render(){
        const { cameraConfig, fullscreen } = this.props;
        if(!cameraConfig){
          return null;
        }
        let videUrlForView = cameraConfig.videoUrl;
        const monitoring = !cameraConfig.disconnected && cameraConfig.monitoringEnabled;
        return(
          <View key={cameraConfig.id}>
            <RTSPVideoPlayer
                  {...this.props}
                  key={cameraConfig.id}
                  url={videUrlForView}
                  monitoring={monitoring}
                  autoPlay={true}
                  title={cameraConfig.name}
                  hideScrubber={true}
                  hideFullScreenControl={false}
                  logo=''
                  hideTime={true}/>
              { !fullscreen && 
                <CameraControl {...this.props} cameraConfig={cameraConfig}/>
              }
          </View>
        )
      }
}

  
export default WatchCamera;