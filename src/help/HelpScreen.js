import React, { Component } from 'react';
import { WebView } from 'react-native-webview';

export default class HelpScreen extends Component{
    render(){
        return(
            <WebView source={{ uri: 'https://aiwatch.live/help.html' }} />
        )
    }
}