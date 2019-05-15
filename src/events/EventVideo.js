import React, {PureComponent} from 'react';
import { StyleSheet, View } from 'react-native';
import { IconButton, Colors } from 'react-native-paper';
import RTSPVideoPlayer from '../cameras/RTSPVideoPlayer';
import Orientation from 'react-native-orientation';

export default class EventVideo extends PureComponent{

    componentDidMount() {
        // this locks the view to Portrait Mode
        Orientation.lockToLandscapeLeft();
    }

    componentWillUnmount() {
        Orientation.lockToPortrait();
    }

    render(){
        //const videoUrl = 'rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov';
        const videoUrl = this.props.navigation.getParam('videoUrl', '');
        return(
            <View style={{flex: 1}}>
                <IconButton icon='close' size={36} 
                    color={Colors.white}
                    style={styles.dismissButton}
                    onPress={() => this.props.navigation.goBack()}/>
                <RTSPVideoPlayer
                    //initWithFull={true}
                    //onLeftPress={() => this.props.navigation.goBack()}
                    autoplay={false}
                    showReload={true}
                    style={{flex: 1}}
                    url={videoUrl}/>
            </View>
        )
    }
}

const styles = StyleSheet.create({
    dismissButton:{
        top: 10,
        right: 10,
        marginRight: 30,
        zIndex: 1,
        position: 'absolute',
    }
});