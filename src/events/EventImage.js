import React, {PureComponent} from 'react';
import { ImageBackground, StyleSheet, Text, View } from 'react-native';
import { Colors } from 'react-native-paper';
import { verticalScale } from 'react-native-size-matters';
import RTSPVideoPlayer from '../cameras/RTSPVideoPlayer';
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
        if(event.videoPath){
          return this.renderVideoItem(event);
        }else if(event.thumbnailPath){
          return this.renderImageItem(event);
        }else{
          return this.renderTextItem(event);
        }
    }
    
    renderVideoItem(event){
        return(
            <RTSPVideoPlayer
                {...this.props}
                key={event.uuid}
                style={styles.thumbnailStyle}
                placeholder={event.thumbnailPath}
                url={event.videoPath}
                autoplay={false} />
        )
    }
      
    renderImageItem(event){
        return(
            <ImageBackground
                source={{isStatic:true, uri: 'file://'+event.thumbnailPath}}
                style={styles.thumbnailStyle}>
            </ImageBackground>
        )
    }

    renderTextItem(event){
        return(
          <Text>{event.message}</Text>
        )
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