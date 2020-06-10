
import React from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { ActivityIndicator } from 'react-native-paper';
import * as Progress from 'react-native-progress';

export default class LoadingSpinner extends React.Component{
  render(){
    const { visible, textContent } = this.props;
    if(!visible){
      return null;
    }
    
    return(
      <View style={styles.spinner}>
        {this.renderActivityIndicator()}
        <View style={[styles.textContainer,]}>
          <Text style={[styles.textContent]}>
            {textContent}
          </Text>
        </View>
      </View>
    )
  }

  renderActivityIndicator(){
    if(this.props.showProgress){
      return(
        <Progress.Circle
           {...this.props}
        />
      )
    }
    return(
      <ActivityIndicator animating={true} size={36} 
                              style={{flex: 1, justifyContent: 'center'}} />
    )
  }
}

const styles = StyleSheet.create({
  spinner: {
    position: 'absolute',
    backgroundColor: 'rgba(0, 0, 0, 0.25)',
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
    justifyContent: 'center',
    alignItems: 'center'
  },
  textContainer: {
    alignItems: 'center',
    bottom: 0,
    flex: 1,
    justifyContent: 'center',
    left: 0,
    position: 'absolute',
    right: 0,
    top: 0
  },
  textContent: {
    fontSize: 20,
    height: 50,
    top: 80
  }
});