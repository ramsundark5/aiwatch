import React, { Component } from 'react';
import { Button, List } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import { DeviceEventEmitter, InteractionManager, ScrollView, StyleSheet, View } from 'react-native';
import Theme from '../common/Theme';
import Logger from '../common/Logger';
import Spinner from 'react-native-loading-spinner-overlay';

const INITIAL_PROGRESS_MESSAGE = 'Scanning for devices..';
export default class ScanCamera extends Component {

    static navigationOptions = {
        headerTitle: 'Scan for cameras',
        headerTintColor: Theme.primary,
        headerTitleStyle: {
          fontSize: 16,
          fontWeight: 'normal',
        }
    };

    state = {
        cameras: [],
        nonCameraDevices: [],
        isLoading: false,
        loadingMessage: INITIAL_PROGRESS_MESSAGE,
    }

    componentDidMount(){
        DeviceEventEmitter.addListener('DEVICE_DISCOVERY_PROGRESS_JS_EVENT', this.onNewProgressEvent);
        DeviceEventEmitter.addListener('DEVICE_DISCOVERY_COMPLETED_JS_EVENT', this.handleDiscoveryResult);
        InteractionManager.runAfterInteractions(() => {
            this.onStartScan();
        });
    }

    onNewProgressEvent = (event) => {
        if(event){
            console.log('event ' + event.message);
            this.setState({loadingMessage: event.message});
        }
    }

    onStartScan(){
        this.setState({isLoading: true});
        try{
            RNSmartCam.discover();
            console.log('discovery started');
        }catch(err){
            Logger.error(err);
        }
        //to be safe, dismiss the spinner after 2 mins
        setTimeout(() => {
            this.setState({isLoading: false, loadingMessage: INITIAL_PROGRESS_MESSAGE});
        }, 1000 * 120);
    }

    handleDiscoveryResult = (discoveryResult) => {
        if(discoveryResult){
            let cameras = discoveryResult.cameras;
            let nonCameraDevices = discoveryResult.nonCameraDevices;
            if(!cameras){
                cameras = [];
            }
            if(!nonCameraDevices){
                nonCameraDevices = [];
            }
            this.setState({cameras: cameras, nonCameraDevices: nonCameraDevices});
        }
    }

    render(){
        const { cameras, nonCameraDevices, isLoading } = this.state;
        return(
            <View style={styles.container}>
                {this.renderSpinner()}
                <ScrollView
                    ref={ref => (this.scrollRef = ref)}
                    style={{ flex: 1 }}>
                    {cameras.map((camera) => (
                        this.renderCamera(camera)
                    ))}
                </ScrollView>
                <ScrollView
                    ref={ref => (this.scrollRef = ref)}
                    style={{ flex: 1 }}>
                    {nonCameraDevices.map((nonCameraDevice) => (
                        this.renderNonCameraDevice(nonCameraDevice)
                    ))}
                </ScrollView>
                <View style={styles.footer}>
                    <Button mode='contained' color={Theme.primary} 
                        disabled={isLoading}
                        onPress={() => this.onStartScan()}>
                        Scan
                    </Button>
                </View>
          </View>
        )
    }

    renderSpinner(){
        const { isLoading, loadingMessage } = this.state;
        if(!isLoading){
            return null;
        }
        console.log('render ' + loadingMessage);
        return(
            <Spinner
                cancelable={true}
                visible={isLoading}
                textContent={loadingMessage} / >
        )
    }
    
    renderCamera(camera){
        const title =  camera.name + " " + camera.vendor + " " + camera.model;
        let videoUrl = camera.h264;
        if(!videoUrl || videoUrl.length < 2){
            videoUrl = jpg;
        }
        return(
            <List.Item title={title} key={title}
                description={videoUrl}
                right={() => this.renderAddCameraButton()} />
        )
    }

    renderNonCameraDevice(device){
        const title = device.publicVendor;
        const description = device.ip;
        return(
            <List.Item title={title} key={device.ip}
                description={description}
                right={() => this.renderScanForCamera()} />
        )
    }

    renderAddCameraButton(camera){
        return(
            <View>

            </View>
        )
    }

    renderScanForCamera(device){
        return(
            <View>

            </View>
        )
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    footer: {
      width: '100%',
      paddingTop: 20,
    }
});