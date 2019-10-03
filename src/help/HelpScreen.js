import React, { Component } from 'react';
import { View } from 'react-native';
import { WebView } from 'react-native-webview';
import LoadingSpinner from '../common/LoadingSpinner';

export default class HelpScreen extends Component{
    state = {
        isLoading: false
    }

    static navigationOptions = {
        headerTitle: 'FAQ',
        headerTintColor: Theme.primary,
        headerTitleStyle: {
          fontSize: 16,
          fontWeight: 'normal',
        }
    };

    showSpinner(){
        this.setState({isLoading: true});
        //to be safe, dismiss the spinner after 2 mins
        setTimeout(() => {
            this.setState({isLoading: false});
        }, 1000 * 120);
    }

    UNSAFE_componentWillUnmount(){
        this.setState({isLoading: false});
    }

    render(){
        const { isLoading } = this.state;
        return(
            <View style={{flex: 1}}>
                <WebView source={{ uri: 'https://aiwatch.live/help.html' }} 
                    onLoadStart={() => this.setState({isLoading: true})}
                    onLoadEnd={() => this.setState({isLoading: false})}/>
                <LoadingSpinner
                    size={30}
                    animated={true}
                    cancelable={true}
                    indeterminate={false}
                    visible={isLoading} />
            </View>
        )
    }
}