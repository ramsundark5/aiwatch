import React, {PureComponent} from 'react';
import { StyleSheet, Text, View } from 'react-native';
import ImageOverlay from 'react-native-image-overlay';
import { Avatar, Colors, TouchableRipple } from 'react-native-paper';
import { verticalScale } from 'react-native-size-matters';
export default class EventImage extends PureComponent{
    render(){
        const { event } = this.props;
        return(
            <View style={[styles.eventContainer]}>
                {this.renderEvent(event)}
            </View>
        )
    }

    renderEvent(event){
        if(event.thumbnailPath || event.videoPath){
          return this.renderImageItem(event);
        }else{
          return this.renderTextItem(event);
        }
    }
    
    renderImageItem(event){
        let playerHeight = verticalScale(120);
        return(
            <ImageOverlay
                source={{isStatic:true, uri: 'file://'+event.thumbnailPath}}
                height={playerHeight}
                overlayAlpha={0}
                containerStyle={{width: '100%', maxWidth: 400}} 
                contentPosition='center'>
               <TouchableRipple rippleColor={Colors.white} onPress={() => this.onPlayVideo()}>
                <Avatar.Icon size={48} 
                    icon='play' color={Colors.red600}
                    style={styles.avatarStyle}/>
               </TouchableRipple>
            </ImageOverlay>
        )
    }
      
    renderTextItem(event){
        return(
          <Text>{event.message}</Text>
        )
    }

    onPlayVideo(){
        const { event } = this.props;
        const videoUri = event.videoPath;
        console.log('play video pressed');
        this.props.navigation.navigate('EventVideo', {
            videoUrl: videoUri
        });
    }
}

const styles = StyleSheet.create({
    eventContainer: {
        flex: 12,
        justifyContent: 'center'
    },
    avatarStyle: {
        backgroundColor: 'transparent', 
        borderColor: Colors.white, 
        borderWidth: 1
    }
});