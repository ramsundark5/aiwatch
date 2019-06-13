import React, {PureComponent} from 'react';
import { View, StyleSheet, Text } from 'react-native';
import moment from 'moment';
import Logger from '../common/Logger';
export default class EventTime extends PureComponent{
    render(){
        const { event } = this.props;
        let formattedTime = '';
        try{
            formattedTime = moment(+event.date).local().format('hh:mm a');
        }catch(err){
            Logger.log('error formatting datetime');
            Logger.error(err);
        }
        return(
            <View style={[styles.timeContainer]}>
                <Text>{formattedTime}</Text>
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