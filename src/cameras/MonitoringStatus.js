import React, { Component } from 'react';
import { View, StyleSheet } from 'react-native';
import RNSmartCam from '../native/RNSmartCam';
import { Caption, Colors, IconButton, Switch, Subheading } from 'react-native-paper';
import Logger from '../common/Logger';
import Theme from '../common/Theme';
import testID from '../common/testID';

export default class MonitoringStatus extends Component{

    componentDidMount(){
        this.checkMonitoringStatus();
    }

    UNSAFE_componentWillReceiveProps(nextProps){
        if(this.props.cameras !== nextProps.cameras){
            this.checkMonitoringStatus();
        }
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
            <View style={styles.container}>
                <View style={styles.row} {...testID('monitorstatus')}>
                    <Subheading style={styles.textStyle}>Monitoring On</Subheading>
                    <Switch accessibilityLabel='togglemonitor'
                        value={isMonitoringOn}
                        onValueChange={value => this.onToggleMonitoring(value)} />
                    <IconButton icon="autorenew"
                        color={Theme.primary}
                        onPress={ () => this.reloadConfigs()}/>
                </View>
                {this.renderWarningMessage()}
            </View>
        )
    }

    renderWarningMessage(){
        const { isMonitoringOn } = this.props;
        if(isMonitoringOn){
            return null;
        }
        return(
            <View style={[styles.row, {marginTop: -16}]}>
                <IconButton
                icon="alert-circle-outline"
                color={Colors.red500}
                size={20} />
                <Caption>Monitoring is off. Click the button above to enable</Caption>
            </View>
        )
    }
}

const styles = StyleSheet.create({
    container: {
        marginRight: 10,
        marginLeft: 10,
        borderRadius:50,
        borderWidth: 1,
        borderColor: Colors.grey500
    },
    row: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'center',
    },
    textStyle:{
        color: Theme.primary
    }
});