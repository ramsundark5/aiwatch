import React, { Component } from 'react';
import { Text, ToastAndroid, View } from 'react-native';

export default class ErrorBoundary extends Component {
    constructor(props) {
      super(props);
      this.state = { hasError: false };
    }
  
    componentDidCatch(error, info) {
      // Display fallback UI
      this.setState({ hasError: true });
      ToastAndroid.showWithGravity('Unexpected error occured.', ToastAndroid.SHORT, ToastAndroid.CENTER);
      // You can also log the error to an error reporting service
      console.log(error, info);
      //this.props.navigateTo('CameraView');
    }
  
    render() {
      if (this.state.hasError) {
        // You can render any custom fallback UI
        return (
            <View>
                <Text>Something went wrong.</Text>
            </View>
        )
      }
      return this.props.children;
    }
}