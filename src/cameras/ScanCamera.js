import React, { Component } from 'react';
import { Button, Divider, List, Title } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import { DeviceEventEmitter, Fragment, InteractionManager, ScrollView, StyleSheet, View } from 'react-native';
import Theme from '../common/Theme';
import Logger from '../common/Logger';
import LoadingSpinner from '../common/LoadingSpinner';

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
        progress: 0
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
            let scanPercentage = event.scanPercentage;
            let progress = scanPercentage ? scanPercentage / 100 : 0;
            this.setState({loadingMessage: event.message, progress: progress});
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
        this.setState({isLoading: false});
        if(discoveryResult){
            let cameras = discoveryResult.cameras;
            let nonCameraDevices = discoveryResult.nonCameraDevices;
            if(!cameras){
                cameras = [];
            }
            if(!nonCameraDevices){
                nonCameraDevices = [];
            }
            this.setState({isLoading: false, cameras: cameras, nonCameraDevices: nonCameraDevices});
        }
    }

    render(){
        const { cameras, nonCameraDevices, isLoading } = this.state;
        return(
            <View style={{flex: 1}}>
                <ScrollView style={styles.container}>
                    {this.renderSpinner()}
                    {this.renderCameras(nonCameraDevices)}
                    {this.renderNonCameraDevices(nonCameraDevices)}
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
        const { isLoading, loadingMessage, progress } = this.state;
        if(!isLoading){
            return null;
        }
        console.log('render ' + loadingMessage);
        return(
            <LoadingSpinner
                size={100}
                animated={true}
                cancelable={true}
                showsText={true}
                showProgress={true}
                progress={progress}
                indeterminate={false}
                visible={isLoading}
                textContent={loadingMessage} / >
        )
    }
    
    renderCameras(cameras){
        if(!cameras || cameras.length < 1){
            return null;
        }
        return(
            <View>
                <Title style={{paddingLeft: 15}}>Identified Cameras:</Title>
                <View
                    ref={ref => (this.scrollRef = ref)}>
                    {cameras.map((camera) => (
                        this.renderNonCameraDevice(camera)
                    ))}
                </View>
            </View>
        )
    }

    renderCamera(camera){
        const title =  camera.name + " " + camera.vendor + " " + camera.model;
        let videoUrl = camera.h264;
        if(!videoUrl || videoUrl.length < 2){
            videoUrl = jpg;
        }
        return(
            <View>
                <Divider /> 
                <List.Item title={title} key={title}
                    description={videoUrl}
                    right={() => this.renderAddCameraButton()} />
            </View>
        )
    }

    renderNonCameraDevices(nonCameraDevices){
        if(!nonCameraDevices || nonCameraDevices.length < 1){
            return null;
        }
        return(
            <View style={{flex: 1, paddingTop: 20}}>
                <Title style={{paddingLeft: 15}}>Network devices:</Title>
                <View
                    ref={ref => (this.scrollRef = ref)}>
                    {nonCameraDevices.map((nonCameraDevice) => (
                        this.renderNonCameraDevice(nonCameraDevice)
                    ))}
                </View>
            </View>
        )
    }

    renderNonCameraDevice(device){
        const title = device.publicVendor;
        const description = device.ip;
        return(
            <View>
                <Divider/>
                <List.Item title={title} key={device.ip}
                    description={description}
                    right={() => this.renderScanForCamera()} />
            </View>
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
      paddingLeft: 10,
      paddingRight: 10,
      paddingBottom: 5
    }
});