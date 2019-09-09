
import React from 'react';
import { ActivityIndicator } from 'react-native-paper';
import Spinner from 'react-native-loading-spinner-overlay';
import * as Progress from 'react-native-progress';

export default class LoadingSpinner extends React.Component{
  render(){
    return(
      <Spinner
            {...this.props}
            customIndicator={this.renderActivityIndicator()}
      />
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