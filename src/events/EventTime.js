import React, {PureComponent} from 'react';
import { View, StyleSheet, Text } from 'react-native';
import moment from 'moment';

export default class EventTime extends PureComponent{
    render(){
        const { event } = this.props;
        let formattedTime = '';
        try{
            formattedTime = moment(+event.date).format('hh:mm a');
        }catch(err){
            console.log('error formatting datetime');
        }
        return(
            <View style={[styles.timeContainer]}>
                <Text>{formattedTime}{event.id}</Text>
            </View>
        )
    }
}

const styles = StyleSheet.create({
    timeContainer: {
      minWidth: 45,
      justifyContent: 'center',
      paddingLeft: 20
    },
});