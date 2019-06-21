import React, { Component } from 'react';
import { View, StyleSheet } from 'react-native';
import RNSmartCam from '../native/RNSmartCam';
import { Button, Colors, IconButton, Switch, Subheading } from 'react-native-paper';
import Logger from '../common/Logger';
import Theme from '../common/Theme';

export default class MonitoringStatus extends Component{

    componentDidMount(){
        this.checkMonitoringStatus();
    }

    async checkMonitoringStatus(){
        try{
            const { updateMonitoringStatus } = this.props;
            let isMonitoringRunning = await RNSmartCam.isMonitoringServiceRunning();
            updateMonitoringStatus(isMonitoringRunning);
        }catch(err){
            Logger.error(err);
        }
    }

    async onToggleMonitoring(enableMonitoring){
        const { updateMonitoringStatus } = this.props;
        try{
          await RNSmartCam.toggleMonitoringStatus(enableMonitoring);
          updateMonitoringStatus(enableMonitoring);
        }catch (err){
          Logger.error(err);
        }
    }

    reloadConfigs(){
        const { loadAllCameras } = this.props;
        loadAllCameras();
        this.checkMonitoringStatus();
    }

    render(){
        const { isMonitoringOn } = this.props;
        return(
            <View style={styles.row}>
                <Subheading style={styles.textStyle}>Monitoring On</Subheading>
                <Switch
                    value={isMonitoringOn}
                    onValueChange={value => this.onToggleMonitoring(value)} />
                <IconButton icon="autorenew"
                    color={Theme.primary}
                    onPress={ () => this.reloadConfigs()}/>
            </View>
        )
    }
}

const styles = StyleSheet.create({
    row: {
      flexDirection: 'row',
      marginRight: 30,
      marginLeft: 30,
      alignItems: 'center',
      justifyContent: 'center',
      borderRadius:50,
      borderWidth: 1,
      borderColor: Colors.grey500
    },
    textStyle:{
        color: Theme.primary
    }
});