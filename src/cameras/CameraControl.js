import React, { Component } from 'react';
import { Alert, StyleSheet, ToastAndroid, View } from 'react-native';
import { Appbar } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import Theme from '../common/Theme';

export default class CameraControl extends Component {

    editCamera(){
        const { cameraConfig, navigation } = this.props;
        navigation.navigate('EditCamera', {
          cameraConfig: cameraConfig
        });
    }

    onPressDeleteButton(){
        const { cameraConfig } = this.props;
        Alert.alert(
          'Delete Camera',
          'Are you sure you want to delete the camera ' + cameraConfig.name.toUpperCase() +' ?',
          [
            {
              text: 'Cancel',
              onPress: () => console.log('Cancel Pressed'),
              style: 'cancel',
            },
            {text: 'OK', onPress: () => this.deleteCamera(cameraConfig)},
          ]
        );
    }
      
    async deleteCamera(){
        const { cameraConfig, deleteCamera } = this.props;
        await RNSmartCam.deleteCamera(cameraConfig.id);
        deleteCamera(cameraConfig.id);
        ToastAndroid.showWithGravity('Camera deleted', ToastAndroid.SHORT, ToastAndroid.CENTER);
    }
    
    async toggleMonitoring(){
        const { cameraConfig } = this.props;
        let camerConfigUpdate = Object.assign({}, cameraConfig);
        camerConfigUpdate.disconnected = !cameraConfig.disconnected;
        await RNSmartCam.togglCameraMonitoring(camerConfigUpdate);
    }

    render(){
        const { cameraConfig, isFull } = this.props;
        if(isFull){
            return null;
        }
        const monitoringEnabled = this.isMonitoringEnabled(cameraConfig);
        const monitoringIcon = !cameraConfig.disconnected && monitoringEnabled ? 'visibility' : 'visibility-off';
        return(
            <View>
               <Appbar style={styles.appBar}>
                <Appbar.Action icon='settings' color={Theme.primary} onPress={() => this.editCamera()} />
                <Appbar.Action icon={monitoringIcon} color={Theme.primary} onPress={() => this.toggleMonitoring()} />
                <Appbar.Action icon='delete' color={Theme.primary} onPress={() => this.onPressDeleteButton()} />
              </Appbar>
              <View style={styles.divider} />
            </View>
        )
    }

    isMonitoringEnabled(cameraConfig){
      let monitoringEnabled = cameraConfig.notifyPersonDetect || cameraConfig.recordPersonDetect
                || cameraConfig.notifyAnimalDetect || cameraConfig.recordAnimalDetect
                || cameraConfig.notifyVehicleDetect || cameraConfig.recordVehicleDetect
                || cameraConfig.testModeEnabled;
      return monitoringEnabled;
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