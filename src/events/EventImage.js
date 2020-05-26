import React, {PureComponent} from 'react';
import { ImageBackground, StyleSheet, Text, View } from 'react-native';
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
            <ImageBackground
                source={{isStatic:true, uri: 'file://'+event.thumbnailPath}}
                style={styles.thumbnailStyle}>
               <TouchableRipple rippleColor={Colors.white} onPress={() => this.onPlayVideo()}>
                <Avatar.Icon size={48} 
                    icon='play' color={Colors.red600}
                    style={styles.avatarStyle}/>
               </TouchableRipple>
            </ImageBackground>
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
    },
    thumbnailStyle: {
        width: '100%', 
        height: verticalScale(120),
        maxWidth: 400, 
        justifyContent: 'center', 
        alignItems: 'center', 
    }
});