import React, {PureComponent} from 'react';
import { StyleSheet, View } from 'react-native';
import { IconButton, Colors } from 'react-native-paper';
import RTSPVideoPlayer from '../cameras/RTSPVideoPlayer';
import Modal from 'react-native-modal';
import Orientation from 'react-native-orientation';
export default class FullScreenVideoPlayer extends PureComponent{

    /* componentDidUpdate(){
        if(this.props.isFull){
            Orientation.lockToLandscapeLeft();
        }
    }
    componentWillUnmount() {
        Orientation.lockToPortrait();
    }
 */
    render(){
        const { isFull, videoUrl, onClose } = this.props;
        return(
            <Modal
                style={styles.container}
                isVisible={isFull}
                onBackButtonPress={() => onClose()}
                onSwipeComplete={() => onClose()}
                swipeDirection='down'>
                <IconButton icon='close' size={36} 
                    color={Colors.white}
                    style={styles.dismissButton}
                    onPress={() => onClose()}/>
                <RTSPVideoPlayer
                    Orientation={Orientation}
                    showTop={false}
                    fullVideoAspectRatio={""}
                    videoAspectRatio={""}
                    //autoplay={true} //don't enable this. video is not playing full screen
                    showReload={false}
                    style={{flex: 1}}
                    url={videoUrl}
                    onLeftPress={() => onClose()}/>
            </Modal>
        )
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        margin: 0
    },
    dismissButton:{
        top: 40,
        right: 30,
        marginRight: 30,
        zIndex: 1,
        position: 'absolute',
    }
});