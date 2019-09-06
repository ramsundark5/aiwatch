import React, { Component } from 'react';
import { Button, Divider, IconButton, List, Subheading, Title, Colors } from 'react-native-paper';
import RNSmartCam from '../native/RNSmartCam';
import { DeviceEventEmitter, InteractionManager, Linking, ScrollView, StyleSheet, ToastAndroid, View } from 'react-native';
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

    onAddCamera(camera){
        if(!camera){
            ToastAndroid.showWithGravity('Invalid camera. Try a different one.', ToastAndroid.SHORT, ToastAndroid.CENTER);
            return;
        }
        this.props.navigation.navigate('EditCamera', {
            cameraConfig: {
              uuid: uuid,
              name: camera.name ,
              brand: camera.vendor,
              model: camera.model,
              videoUrl: camera.h264,
              username: camera.username,
              password: camera.password
            }
        });
    }

    onPressURLFinderButton(){
        Linking.openURL('https://www.ispyconnect.com/sources.aspx');
    }

    render(){
        const { cameras, nonCameraDevices, isLoading } = this.state;
        return(
            <View style={{flex: 1}}>
                <ScrollView style={styles.container}>
                    {this.renderSpinner()}
                    {this.renderCameras(cameras)}
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
            const demoCamera = {
                    name: 'demo',
                    vendor: 'test',
                    model: 'test',
                    h264: 'test',
                    username: 'test',
                    password: 'test'
            }
            cameras = [demoCamera];
        }
        return(
            <View>
                <Title style={{paddingLeft: 15}}>Identified Cameras:</Title>
                <View
                    ref={ref => (this.scrollRef = ref)}>
                    {cameras.map((camera) => (
                        this.renderCamera(camera)
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
                    right={() => this.renderAddCameraButton(camera)} />
            </View>
        )
    }


    renderAddCameraButton(camera){
        return(
            <IconButton
                icon='chevron-right'
                color={Colors.grey500}
                size={30}
                onPress={() => this.onAddCamera(camera)}
            />
        )
    }

    renderNonCameraDevices(nonCameraDevices){
        if(!nonCameraDevices || nonCameraDevices.length < 1){
            return null;
        }
        return(
            <View style={{flex: 1, paddingTop: 20}}>
                <Title style={{paddingLeft: 15}}>Network devices:</Title>
                <Subheading>We cannot confirm if the below are cameras. Ask your manufacturer or try camera database like  
                    <Button color={Theme.primary} 
                        onPress={() => this.onPressURLFinderButton()} uppercase={false}>
                        https://www.ispyconnect.com/sources.aspx
                    </Button>
                </Subheading>
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
                    description={description}/>
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