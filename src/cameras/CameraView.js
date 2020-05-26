import React, { Component } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Button, Colors, FAB, Headline, Portal, Provider } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import { loadCameras, deleteCamera, updateMonitoringStatus, updateStatus } from '../store/CamerasStore';
import { connect } from 'react-redux';
import Logger from '../common/Logger';
import MonitoringStatus from './MonitoringStatus';
import AiwatchUtil from '../common/AiwatchUtil';
import LoadingSpinner from '../common/LoadingSpinner';
import Theme from '../common/Theme';
import testID from '../common/testID';
import { FlatGrid } from 'react-native-super-grid';
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

  render() {
    const { isFull, isLoading } = this.state;
    return (
      <Provider>
         <Portal>
            <LoadingSpinner 
              visible={isLoading}
              textContent={'Loading...'} />
            <View {...testID('cameraHome')} style={[styles.container, { marginTop: isFull ? 0 : 20 }]}>
              <MonitoringStatus loadAllCameras={() => this.loadAllCameras()} {...this.props}/>
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
    if(cameras && cameras.length === 1){
      let cameraConfigProp = cameras[0];
      return (
        <ScrollView style={{flex: 1}}>
          {this.renderSingleCamera(cameraConfigProp)}
        </ScrollView>
      )
    }
    //render multiple cameras in grid
    let playerSize = 300;
    if(this.deviceType == 'Small_Tablet'){
      playerSize = 400;
    }
    if(this.deviceType == 'Large_Tablet'){
      playerSize = 600;
    }
    return(
      <FlatGrid
        spacing={0}
        itemDimension={playerSize}
        items={cameras}
        renderItem={({ item, index }) => this.renderSingleCamera(item, index)}/>
    )
  }

  renderSingleCamera(cameraConfigProp){
    return(
      <WatchCamera cameraConfig={cameraConfigProp} />
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
    if(this.state.isFull){
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
    if(this.state.isFull){
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
    width: '100%',
    padding: 10,
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
