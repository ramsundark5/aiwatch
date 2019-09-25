import React, {PureComponent} from 'react';
import { View, StyleSheet } from 'react-native';
import { Avatar } from 'react-native-paper';
import { verticalScale } from 'react-native-size-matters';
const DEFAULT_LINE_WIDTH = 1;
const DEFAULT_LINE_COLOR = 'lightblue';

export default class EventIcon extends PureComponent{
    render(){
        const { event } = this.props;
        let iconName = 'walk';
        if(event.message && event.message.toLowerCase().includes('vehicle')){
            iconName = 'car';
        }else if(event.message && event.message.toLowerCase().includes('animal')){
            iconName = 'paw';
        }
        return(
            <View style={[styles.separatorContainer]}>
                <View style={[styles.line, { width: DEFAULT_LINE_WIDTH, backgroundColor: DEFAULT_LINE_COLOR }]} />
                    <Avatar.Icon size={32} icon={iconName} />
                <View style={[styles.line, { width: DEFAULT_LINE_WIDTH, backgroundColor: DEFAULT_LINE_COLOR }]} />
            </View>
        )
    }
}

const styles = StyleSheet.create({
    separatorContainer: {
      minHeight: verticalScale(150),//configure this adjust spacing between events
      alignItems: 'center',
      paddingLeft: '10%',
      paddingRight: '10%'
    },
    line: {
      flex: 1,
      backgroundColor: 'black',
      marginHorizontal: 8
    },
});