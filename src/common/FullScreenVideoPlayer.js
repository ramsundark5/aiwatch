import React, {PureComponent} from 'react';
import { StyleSheet, View } from 'react-native';
import { IconButton, Colors } from 'react-native-paper';
import RTSPVideoPlayer from '../cameras/RTSPVideoPlayer';
import Orientation from 'react-native-orientation-locker'
import { VlCPlayerView } from 'react-native-vlc-media-player';
export default class FullScreenVideoPlayer extends PureComponent{

    static navigationOptions = {
        header: null,
        tabBarVisible: false,
    };
      
    componentDidMount(){
        Orientation.lockToLandscapeLeft()
    }
    
    onBackPress(){
        this.props.navigation.goBack()
    }

    render(){
        const videoUrl = this.props.navigation.getParam('videoUrl', '');
        return(
            <View style={{flex: 1, backgroundColor: 'black'}}>
                <IconButton icon='close' size={36} 
                    color={Colors.white}
                    style={styles.dismissButton}
                    onPress={() => this.props.navigation.goBack()}/>
                <RTSPVideoPlayer
                    monitoring=''
                    autoPlay={true}
                    onLeftPress={() => this.props.navigation.goBack()}
                    style={{flex: 1}}
                    autoAspectRatio={true}
                    hideScrubber={false}
                    hideFullScreenControl={true}
                    url={videoUrl}/>
            </View>
        )
    }

    render2(){
        const videoUrl = this.props.navigation.getParam('videoUrl', '');
        return(
            <VlCPlayerView
                autoplay={false}
                url={videoUrl}
                Orientation={Orientation}
                closeFullScreen={() => this.onBackPress()}
                //BackHandle={BackHandle}
                isFull={true}
                showLeftButton={false}
                showRightButton={false}
                ggUrl=""
                showGG={false}
                title=""
                showBack={true}
            />
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