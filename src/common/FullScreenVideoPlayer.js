import React, {PureComponent} from 'react';
import { StyleSheet, View } from 'react-native';
import { IconButton, Colors } from 'react-native-paper';
import RTSPVideoPlayer from '../cameras/RTSPVideoPlayer';

export default class FullScreenVideoPlayer extends PureComponent{

    static navigationOptions = {
        header: null,
        tabBarVisible: false,
    };
      
    render(){
        const videoUrl = this.props.navigation.getParam('videoUrl', '');
        return(
            <View style={{flex: 1, backgroundColor: 'black'}}>
                <IconButton icon='close' size={36} 
                    color={Colors.white}
                    style={styles.dismissButton}
                    onPress={() => this.props.navigation.goBack()}/>
                <RTSPVideoPlayer
                    initWithFull={true}
                    showFullScreen={true}
                    onLeftPress={() => this.props.navigation.goBack()}
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