import React, { Component } from 'react';
import { Alert, StyleSheet, ToastAndroid, View } from 'react-native';
import { Appbar } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import Theme from '../common/Theme';
import Logger from '../common/Logger';
import LoadingSpinner from '../common/LoadingSpinner';
export default class CameraControl extends Component {

    state = {
      isLoading: false
    }

    editCamera(){
        const { cameraConfig, navigation } = this.props;
        navigation.navigate('EditCamera', {
          cameraConfig: cameraConfig
        });
    }

    onPressDeleteButton(){
        const { cameraConfig } = this.props;
        try{
          let camerName = cameraConfig.name;
          if(camerName){
            camerName = camerName.toUpperCase();
          }
          Alert.alert(
            'Delete Camera',
            'Are you sure you want to delete the camera ' + camerName +' ?',
            [
              {
                text: 'Cancel',
                onPress: () => console.log('Cancel Pressed'),
                style: 'cancel',
              },
              {text: 'OK', onPress: () => this.deleteCamera(cameraConfig)},
            ]
          );
        }catch(err){
          Logger.error(err);
        }
    }
    
    onPressToggleMonitorButton(){
      const { cameraConfig } = this.props;
      const isDisconnectRequested = !cameraConfig.disconnected;
      let title = 'Enable monitoring';
      let description = 'Do you want to start monitoring the camera at '+ cameraConfig.name +'?';
      if(isDisconnectRequested){
        title = 'Disable monitoring';
        description = 'Do you want to stop monitoring the camera at '+ cameraConfig.name +'?';
      }
      Alert.alert(
        title,
        description,
        [
          {
            text: 'Cancel',
            onPress: () => console.log('Cancel Pressed'),
            style: 'cancel',
          },
          {text: 'OK', onPress: () => this.toggleMonitoring(isDisconnectRequested)},
        ]
      );
    }

    onPressROIButton(){
      const { cameraConfig, navigation } = this.props;
      navigation.navigate('RegionOfInterest', {
        cameraConfig: cameraConfig
      });
    }

    async deleteCamera(){
        const { cameraConfig, deleteCamera } = this.props;
        await RNSmartCam.deleteCamera(cameraConfig.id);
        deleteCamera(cameraConfig.id);
        ToastAndroid.showWithGravity('Camera deleted', ToastAndroid.SHORT, ToastAndroid.CENTER);
    }
    
    async toggleMonitoring(isDisconnectRequested){
        const { cameraConfig } = this.props;
        this.setState({isLoading: true});
        try{
          let camerConfigUpdate = Object.assign({}, cameraConfig);
          camerConfigUpdate.disconnected = isDisconnectRequested;
          await RNSmartCam.togglCameraMonitoring(camerConfigUpdate);
        }catch(err){
          Logger.log('error toggling monitor status '+err);
        }finally{
          this.setState({isLoading: false});
        }
    }

    render(){
        const { cameraConfig, isFull } = this.props;
        if(isFull){
            return null;
        }
        const monitoringEnabled = cameraConfig.monitoringEnabled;//this.isMonitoringEnabled(cameraConfig);
        const monitoringIcon = !cameraConfig.disconnected && monitoringEnabled ? 'eye' : 'eye-off';
        return(
            <View>
              <LoadingSpinner
                visible={this.state.isLoading}
                textContent={'Updating...'} />
               <Appbar style={styles.appBar}>
                <Appbar.Action icon='settings' color={Theme.primary} onPress={() => this.editCamera()} />
                <Appbar.Action icon='crop' color={Theme.primary} onPress={() => this.onPressROIButton()}/>
                <Appbar.Action icon={monitoringIcon} color={Theme.primary} onPress={() => this.onPressToggleMonitorButton()}/>
                <Appbar.Action icon='delete' color={Theme.primary} onPress={() => this.onPressDeleteButton()} />
              </Appbar>
              <View style={styles.divider} />
            </View>
        )
    }
}

const styles = StyleSheet.create({
    appBar:{
      justifyContent: 'space-around',
      backgroundColor: Theme.grey,
      marginRight: 2,
      marginLeft: 2,
      borderRadius: 10,
      height: 30
    },
    divider: {
      width: '100%',
      height: 20,
    }
  });