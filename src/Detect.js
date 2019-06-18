/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, { Component } from 'react';
import {
  TextInput,
  StyleSheet,
  Text,
  View,
  TouchableHighlight,
  Button,
  ToastAndroid,
  Image
} from 'react-native';
import RNSmartCam from './native/RNSmartCam';
import { NativeEventEmitter } from 'react-native';

export default class Detect extends Component {
  constructor(props) {
    super(props);
    this.subscription = null;
    this.state = {
      personDetected: '',
      vehicleDetected: '',
      animalDetected: '',
      lastDetectedObject: '',
      otherDetected: '',
      lastDetecedTime: new Date().getTime(),
      currentTime: '',
      //videoUrl: 'rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov',
      videoUrl: 'rtsp://ramsundark5.ddns.net:554/cam/realmonitor?channel=3&subtype=0',
      capturedImage: ''
    };
  }

  componentDidMount() {
    const SMARTCAM_EVENT = new NativeEventEmitter(RNSmartCam);
    this.personDetectSubscription = SMARTCAM_EVENT.addListener(
      'PERSON_DETECTED_EVENT',
      confidence => {
        let personDetectMessage = 'Person ' + confidence + ' at ' + new Date().toLocaleTimeString();
        var timeSinceLastDetect = (new Date().getTime() - this.state.lastDetecedTime) / 1000;
        this.setState({
          personDetected: personDetectMessage,
          lastDetecedTime: new Date().getTime()
        });
        if (timeSinceLastDetect > 5) {
          ToastAndroid.show(personDetectMessage, ToastAndroid.SHORT);
        }
      }
    );
    this.animalDetectSubscription = SMARTCAM_EVENT.addListener(
      'ANIMAL_DETECTED_EVENT',
      confidence =>
        this.setState({
          animalDetected: 'Animal ' + confidence + ' at ' + new Date().toLocaleTimeString()
        })
    );
    this.vehicleDetectSubscription = SMARTCAM_EVENT.addListener(
      'ANIMAL_DETECTED_EVENT',
      confidence =>
        this.setState({
          vehicleDetected: 'Vehicle ' + confidence + ' at ' + new Date().toLocaleTimeString()
        })
    );
    this.otherDetectSubscription = SMARTCAM_EVENT.addListener('OTHER_DETECTED_EVENT', confidence =>
      this.setState({
        personDetected: '',
        vehicleDetected: '',
        animalDetected: '',
        otherDetected: 'other ' + new Date().toLocaleTimeString()
      })
    );

    this.imageReceivedSubscription = SMARTCAM_EVENT.addListener('CAPTURED_IMAGE', imageUrl =>
      this.setState({ capturedImage: imageUrl })
    );

    this._interval = setInterval(() => {
      this.setState({ currentTime: new Date().toLocaleString() });
    }, 1000);
  }

  componentWillUnmount() {
    this.personDetectSubscription.remove();
    this.animalDetectSubscription.remove();
    this.vehicleDetectSubscription.remove();
    this.otherDetectSubscription.remove();
    clearInterval(this._interval);
  }

  objectDetectionListener(message) {
    this.setState({
      lastDetectedObject: message
    });
  }

  detect() {
    try {
      RNSmartCam.detectObjects(this.state.videoUrl);
    } catch (err) {
      console.log('error detecting image ' + err);
    }
  }

  stop() {
    try {
      RNSmartCam.stopDetecting(this.state.videoUrl);
    } catch (err) {
      console.log('error stopping detection ' + err);
    }
  }

  render() {
    let { personDetected, otherDetected, capturedImage, currentTime, videoUrl } = this.state;
    return (
      <View style={styles.container}>
        <TextInput
          style={{
            height: 40,
            width: '80%',
            borderColor: 'gray',
            borderWidth: 1
          }}
          onChangeText={videoUrlValue => this.setState({ videoUrl: videoUrlValue })}
          value={videoUrl}
        />
        <TouchableHighlight>
          <Button
            onPress={() => this.detect()}
            style={styles.button}
            title="Detect Objects"
            accessibilityLabel="Learn more about this purple button"
          />
        </TouchableHighlight>
        <TouchableHighlight>
          <Button
            onPress={() => this.stop()}
            style={styles.button}
            title="Stop Detecting"
            accessibilityLabel="Learn more about this purple button"
          />
        </TouchableHighlight>
        <Text style={styles.instructions}>{personDetected}</Text>
        <Text style={styles.instructions}>{otherDetected}</Text>
        <Text>No animal detected</Text>
        <Text style={styles.instructions}>{currentTime}</Text>
        <Text style={styles.instructions}>{capturedImage}</Text>

        {/* <Image 
            key={currentTime}
            style={{height: 300, width: '100%'}}
            source={{isStatic:true, uri: 'file://'+capturedImage + '?' + currentTime}}
          /> */}
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'white',
    padding: 2
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5
  },
  button: {
    color: '#841584'
  }
});
