import React, {PureComponent} from 'react';
import { View, StyleSheet } from 'react-native';
import { Avatar } from 'react-native-paper';

const DEFAULT_LINE_WIDTH = 1;
const DEFAULT_LINE_COLOR = 'lightblue';

export default class EventIcon extends PureComponent{
    render(){
        const { event } = this.props;
        let iconName = 'directions-walk';
        if(event.message === 'VEHICLE_DETECTED_EVENT'){
            iconName = 'directions-car';
        }else if(event.message === 'ANIMAL_DETECTED_EVENT'){
            iconName = 'pets';
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
      minHeight: 150,//configure this adjust spacing between events
      alignItems: 'center',
      paddingLeft: '15%',
      paddingRight: '10%'
    },
    line: {
      flex: 1,
      backgroundColor: 'black',
      marginHorizontal: 8
    },
});