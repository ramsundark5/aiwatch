import React, { Component } from 'react';
import { Button, List, ActivityIndicator } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import { DeviceEventEmitter, InteractionManager, ScrollView, StyleSheet, Text, View } from 'react-native';
import Theme from '../common/Theme';
import Logger from '../common/Logger';
import Spinner from 'react-native-loading-spinner-overlay';
import LoadingSpinner from '../common/LoadingSpinner';
import * as Progress from 'react-native-progress';

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
        loadingMessage: 'Scanning for devices..',
        progress: 0,
        indeterminate: true,
    }

    componentDidMount(){
        DeviceEventEmitter.addListener('DEVICE_DISCOVERY_PROGRESS_JS_EVENT', this.onNewProgressEvent);
        InteractionManager.runAfterInteractions(() => {
            this.onStartScan();
        });
    }

    showProgress() {
        let progress = 0;
        let durationOfProgressbar = 30 * 1000; //60 seconds
        let startTime = new Date().getTime();
        this.setState({ progress: 0, indeterminate: false });
        let progressInterval = setInterval(() => {
            let currentTime = new Date().getTime();
            let elapsedTime = Math.round( currentTime - startTime);
            progress = Math.round(elapsedTime / durationOfProgressbar);
            if (progress > 1) {
                progress = 1;
                clearInterval(progressInterval);
            }
            this.setState({ progress });
        }, 300);
    }

    onNewProgressEvent = (event) => {
        if(event){
            console.log('event ' + event.message);
            this.setState({loadingMessage: event.message});
            this.forceUpdate();
        }
    }

    onStartScan(){
        this.setState({isLoading: true});
        setTimeout(() => {
            this.scan();
        }, 100);
        setTimeout(() => {
            this.setState({isLoading: false, loadingMessage: 'Scanning for devices..'});
        }, 1000 * 60);
    }

    async scan(){
        try{
            let discoveryResult = await RNSmartCam.discover();
            console.log('discovery completed');
            this.handleDiscoveryResult(discoveryResult);
        }catch(err){
            Logger.error(err);
        }finally{  
            this.setState({isLoading: false, loadingMessage: 'Scanning for devices..'});
        }
    }

    handleDiscoveryResult(discoveryResult){
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
            <Progress.Circle
                style={styles.progress}
                progress={this.state.progress}
                indeterminate={this.state.indeterminate}
            />
        )
        /* return(
            <Spinner
                cancelable={true}
                visible={isLoading}
                textContent={loadingMessage} / >
        ) */
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