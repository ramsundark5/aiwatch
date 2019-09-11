import * as React from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { Agenda } from 'react-native-calendars';
import EventCard from './EventCard';
import RNSmartCam from '../native/RNSmartCam';
import { Button } from 'react-native-paper';
import moment from 'moment';
import _ from 'lodash';
import { loadEvents, deleteSelectedEvents, toggleEventSelection } from '../store/EventsStore';
import { connect } from 'react-redux';
import LoadingSpinner from '../common/LoadingSpinner';
import Logger from '../common/Logger';
import AdMob from '../common/AdMob';
import { withNavigation } from 'react-navigation';

class EventsView extends React.Component {

    constructor(props, context) {
        super(props, context);
        this.spinnerCache = new Set();
    }

    static navigationOptions = {
        header: null,
    }

    state = {
        loading: false,
    }

    async componentDidMount(){
        const { navigation } = this.props;
        this.loadInitialEvents();
        this.focusListener = navigation.addListener('didFocus', () => this.showAd());
    }
    
    componentWillUnmount() {
        // Remove the event listener
        this.focusListener.remove();
    }

    async showAd(){
        const { isNoAdsPurchased } = this.props;
        this._addSpinner('ads');
        await AdMob.showAd(isNoAdsPurchased);
        this._removeSpinner('ads');
    }

    loadInitialEvents(){
        let startDate = moment().local().subtract(30,'d');
        let currentDate = moment().local();
        this.loadEventsForDateRange(startDate, currentDate);
    }

    async loadEventsForDateRange(startDate, endDate){
        const { loadEvents } = this.props;
        this._addSpinner('loadevents');
        try{
            let events = await RNSmartCam.getEventsForDateRange({startDate: startDate.valueOf(), endDate: endDate.valueOf()});
            loadEvents(events);
        }catch(err){
            Logger.log('error getting events ');
            Logger.error(err);
        }finally{
            this._removeSpinner('loadevents');
        }
    }
    
    updateSelectedEventsInEditMode(eventId){
        const { editMode, toggleEventSelection } = this.props;
        if(editMode){
            toggleEventSelection(eventId);
        }
    }

    updateSelectedEvents(eventId){
        const { toggleEventSelection } = this.props;
        toggleEventSelection(eventId);
    }

    async deleteEvents(){
        const { events, deleteSelectedEvents } = this.props;
        this._addSpinner('deleteevents');
        try{
            const eventsToDelete = events.filter(event => event.selected === true);
            await RNSmartCam.deleteEvents(eventsToDelete);
            deleteSelectedEvents();
        }catch(err){
            Logger.error(err);
        }finally{
            this._removeSpinner('deleteevents');
        }
    }

    _addSpinner(spinner) {
        this.spinnerCache.add(spinner);
        if(this.spinnerCache.size > 0 ){
            this.setState({loading: true});
        }
    }
    
    _removeSpinner(spinnerToRemove) {
        this.spinnerCache.forEach(spinner => {
          if (spinner === spinnerToRemove) {
            this.spinnerCache.delete(spinner);
          }
        });
        if(this.spinnerCache.size <=0 ){
            this.setState({loading: false});
        }
    }

    render(){
        const { loading } = this.state;
        const { events } = this.props;
        let groupedEvents = {};
        if(events && events.length > 0){
            groupedEvents = _.groupBy(events, function (event) {
                //+ prefix in moment constructor used to avoid deprecation warning. https://stackoverflow.com/questions/39969570/deprecation-warning-in-moment-js
                return moment(+event.date).local().startOf('day').format('YYYY-MM-DD');
            });
        }
        return(
            <View style={{flex: 1, backgroundColor: 'white'}}>
                <LoadingSpinner
                    visible={loading}
                    textContent={'Loading...'} />
                <Agenda items={groupedEvents}
                    renderItem={(event, firstEventInDay) => this.renderEventCard(event, firstEventInDay)}
                    // specify how each date should be rendered. day can be undefined if the item is not first in that day.
                    renderDay={(day, item) => {return (<View />);}}
                    // callback that gets called when items for a certain month should be loaded (month became visible)
                    //loadItemsForMonth={(dateObj) => this.loadEventsForMonth(dateObj)}
                    // If provided, a standard RefreshControl will be added for "Pull to Refresh" functionality. Make sure to also set the refreshing prop correctly.
                    onRefresh={() => this.loadInitialEvents()}
                    // Set this true while waiting for new data from a refresh
                    refreshing={loading}
                    // specify how empty date content with no items should be rendered
                    renderEmptyDate={() => {return (<View/>);}}
                      // specify what should be rendered instead of ActivityIndicator
                    renderEmptyData = {() => this.renderEmptyData()}
                    rowHasChanged={(r1, r2) => this.rowHasChanged(r1, r2)}/>
                    
                    {this.renderDeleteButton()}
            </View>
        )
    }

    renderEventCard(event, firstEventInDay){
        const eventStyle = firstEventInDay ? styles.firstEventPadding : styles.emptyStyle;
        console.log('render eventcard is called' + this.state);
        return(
            <View style={[eventStyle]}>
                <EventCard 
                    {...this.props}
                    key={event.id}
                    event={event} 
                    updateSelectedEventsInEditMode={(eventId) => this.updateSelectedEventsInEditMode(eventId)}
                    updateSelectedEvents={(eventId) => this.updateSelectedEvents(eventId)}/>
            </View>
        )
    }

    renderDeleteButton(){
        const { editMode } = this.props;
        if(!editMode){
          return null;
        }
        return(
          <View style={styles.deleteButtonStyle}>
            <Button icon="delete" mode="contained" onPress={() => this.deleteEvents()}>
              Delete
            </Button>
          </View>
        )
    }

    renderEmptyData(){
        return (
            <View style={styles.emptyData}>
                <Text style={{textAlign: 'center'}}>No events found for this day. Swipe down the calendar and look for a day that has the dot marking.</Text>
            </View>
        )
    }

    rowHasChanged(event1, event2){
        let isChanged = (event1.id !== event2.id || event1.selected !== event2.selected);
        return isChanged;
    }
}

const mapStateToProps = state => ({
    events: state.events.events,
    editMode: state.events.editMode,
    isNoAdsPurchased: state.settings.isNoAdsPurchased
});
  
const connectedEventsView = connect(
    mapStateToProps,
    { loadEvents, deleteSelectedEvents, toggleEventSelection }
)(EventsView);

export default withNavigation(connectedEventsView);

const styles = StyleSheet.create({
    firstEventPadding: {
      paddingTop: 15,
    },
    emptyStyle: {

    },
    deleteButtonStyle:{
        bottom: 0,
        padding: 2,
        zIndex: 1,
        position: 'absolute',
        width: '100%'
    },
    emptyData:{
        justifyContent: 'center',
        alignItems: 'center',
        paddingTop: 40,
        paddingLeft: 10,
        paddingRight: 10
    }
});