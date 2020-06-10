import React, { Component } from 'react';
import { FlatList, StyleSheet, View } from 'react-native';
import { Button, Colors, FAB, Headline, Portal, Provider } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import { loadCameras, deleteCamera, updateMonitoringStatus, updateStatus } from '../store/CamerasStore';
import { connect } from 'react-redux';
import Logger from '../common/Logger';
import AiwatchUtil from '../common/AiwatchUtil';
import LoadingSpinner from '../common/LoadingSpinner';
import Theme from '../common/Theme';
import testID from '../common/testID';
import WatchCamera from './WatchCamera';

class CameraView extends Component {
  
  constructor(props) {
    super(props);
    this.deviceType = null; 
  }

  static navigationOptions = {
    header: null,
  };
 
  state = {
    isFull: false,
    open: false,
    isLoading: false,
    fullscreen: false, 
    fullscreenCameraId: null
  }

  componentDidMount(){
    this.loadAllCameras();
  }

  async loadAllCameras(){
    const { loadCameras } = this.props;
    this.setState({isLoading: true});
    try{
      this.deviceType = await RNSmartCam.getDeviceInfo();
      let cameras = await RNSmartCam.getAllCameras();
      loadCameras(cameras);
    }catch(err){
      Logger.error(err);
    }finally{
      this.setState({isLoading: false});
    }
  }

  onAddCamera(){
    const uuid = AiwatchUtil.uuidv4();
    this.props.navigation.navigate('EditCamera', {
      cameraConfig: {
        uuid: uuid
      }
    });
  }

  onScanCamera(){
    this.props.navigation.navigate('ScanCamera');
  }

  onFullScreen(status, cameraId){
    this.setState({fullscreen: status, fullscreenCameraId: cameraId});
    this.props.navigation.setParams({
      fullscreen: status
    })
  }

  render() {
    const { fullscreen, isLoading } = this.state;
    let containerStyle = fullscreen ? styles.fullscreencontainer : styles.container; 
    return (
      <Provider>
         <Portal>
            <LoadingSpinner 
              visible={isLoading}
              textContent={'Loading...'} />
            <View {...testID('cameraHome')} style={containerStyle}>
              {this.renderEmptyMessage()}
              {this.renderCameras()}
              {this.renderAddCameraButton()}
            </View>
          </Portal>
      </Provider>
    );
  }

  renderCameras(){
    const { cameras } = this.props;
    const { fullscreen } = this.state;
    let contentPaddingBottom = 60;
    let itemSpacing = 5;
    if(fullscreen){
      contentPaddingBottom = 0;
      itemSpacing = 0;
    }
    return(
      <FlatList
        contentContainerStyle={{ paddingBottom: contentPaddingBottom }}
        spacing={itemSpacing}
        data={cameras}
        keyExtractor={item => 'key'+item.id}
        renderItem={({ item, index }) => this.renderSingleCamera(item)}/>
    )
  }

  renderSingleCamera(cameraConfigProp){
    const { fullscreen, fullscreenCameraId } = this.state;
    if(fullscreen && fullscreenCameraId != cameraConfigProp.id){
      return null;
    }
    return(
        <WatchCamera cameraConfig={cameraConfigProp} key={cameraConfigProp.id}
            fullscreen={fullscreen}
            onFullScreen={(status, cameraId) => this.onFullScreen(status, cameraId)}
            {...this.props}/>
    )
  }

  renderEmptyMessage(){
    const { cameras } = this.props;
    if(cameras && cameras.length > 0){
      return null;
    }
    return(
      <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
        <Headline>No cameras found :( </Headline>
        <Headline>Add a camera to monitor.</Headline>
      </View>
    )
  }

  renderAddCameraButton(){
    const { fullscreen } = this.state;
    if(fullscreen){
      return null;
    }
    const addCameraButton = { icon: 'video-plus', label: 'Add Camera', color: 'white', 
            style: {backgroundColor: Theme.primary}, onPress: () => this.onAddCamera(),
            accessibilityLabel: 'ADD_MANUAL_CAMERA'};

    const scanButton = { icon: 'router-wireless', label: 'Scan', color: 'white', 
            style: {backgroundColor: Theme.primary}, onPress: () => this.onScanCamera(), 
            accessibilityLabel: 'SCAN_CAMERA'};
                        
    return(
        <FAB.Group
          {...testID('ADD_CAMERA_FAB')}
          open={this.state.open}
          color='white'
          fabStyle={{backgroundColor: Theme.primary}}
          icon='plus'
          actions={[ addCameraButton, scanButton ]}
          onStateChange={({ open }) => this.setState({ open })}
          theme={{colors: {text: Colors.black, backdrop: 'transparent'}}}
        />
    )
  }

  renderAddCameraButtonForTest(){
    const { fullscreen } = this.state;
    if(fullscreen){
      return null;
    }
    return(
      <View style={styles.footer}>
        <Button mode='outlined' color={Theme.primary} onPress={() => this.onAddCamera()} {...testID('ADD_CAMERA_BUTTON')}>
          Add Camera
        </Button>
      </View>
    )
  }
}

const mapStateToProps = state => ({
  cameras: state.cameras.cameras,
  isMonitoringOn: state.cameras.isMonitoringOn
});

export default connect(
  mapStateToProps,
  { loadCameras, deleteCamera, updateMonitoringStatus, updateStatus }
)(CameraView);

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 3,
    marginTop: 20
  },
  fullscreencontainer: {
    flex: 1,
    padding: 0,
    marginTop: 0
  },
  footer: {
    width: '100%',
    paddingTop: 20,
  },
  responsizeVideo:{
    alignSelf: 'center', 
    maxWidth: 640
  }
});
