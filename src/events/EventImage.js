import React, {PureComponent} from 'react';
import { StyleSheet, Text, View } from 'react-native';
import ImageOverlay from 'react-native-image-overlay';
import { Avatar, Colors, TouchableRipple } from 'react-native-paper';
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
        return(
            <ImageOverlay
                source={{isStatic:true, uri: 'file://'+event.thumbnailPath}}
                height={120}
                overlayAlpha={0}
                containerStyle={{width: '100%'}} 
                contentPosition='center'>
               <TouchableRipple rippleColor={Colors.white} onPress={() => this.onPlayVideo()}>
                <Avatar.Icon size={48} 
                    icon='play-arrow' color={Colors.red600}
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
        console.log('play video pressed');
        this.props.navigation.navigate('EventVideo', {
            videoUrl: event.videoPath
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