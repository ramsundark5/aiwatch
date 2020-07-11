import React from "react";
import { StyleSheet, Text, View } from "react-native";
import VideoPlayer from '../videoplayer';

class RTSPVideoPlayer extends React.PureComponent{

  constructor(props){
    super(props);
    this.state = {
      loadPlayer: true
    }
  }

  componentDidMount(){
    this.initListeners();
  }

  componentWillUnmount(){
    this.removeListeners();
  }

  initListeners(){
    const { navigation } = this.props;
    if(!navigation){
      console.log('no navigation prop found');
      return;
    }
    this.didFocusSubscription = navigation.addListener( 'willFocus',
      () => {
        this.setState({loadPlayer: true});
      }
    );
    this.didBlurSubscription = navigation.addListener( 'willBlur',
      () => {
        this.setState({loadPlayer: false});
      }
    );
  }

  removeListeners(){
    if(this.didFocusSubscription){
      this.didFocusSubscription.remove();
    }
    if(this.didBlurSubscription){
      this.didBlurSubscription.remove();
    }
  }

  onError = async(err) => {
    const { enableHLSLiveView } = this.props;
    console.log('error loading video ' + JSON.stringify(err));
  };

  onFullScreenEvent(status) {
    const { onFullScreen, cameraConfig } = this.props;
    if(onFullScreen){
      let cameraId = cameraConfig.id;
      if(!status){
        cameraId = null;
      }
      onFullScreen(status, cameraId);
    }
  }

  render(){
    if(!this.state.loadPlayer){
      return null;
    }
    return(
        <VideoPlayer 
          {...this.props}
          ref={(ref) => { this.videoRef = ref }}
          onError={this.onError}
          hideFullScreenControl={false}
          onFullScreen={status => this.onFullScreenEvent(status)}
          rotateToFullScreen={true}
          lockPortraitOnFsExit={true}
          scrollBounce={true}/>
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