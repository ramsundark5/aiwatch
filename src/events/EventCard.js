import * as React from 'react';
import { View, StyleSheet, TouchableOpacity } from 'react-native';
import EventTime from './EventTime';
import EventIcon from './EventIcon';
import EventImage from './EventImage';

export default class EventCard extends React.Component {

  render() {
    const { event } = this.props;
    const itemStyle = event.selected? styles.highlighted : styles.unhighlighted;
    return (
      <View style={styles.container}>
        <TouchableOpacity
            style={[styles.rowContainer, itemStyle]}
            onPress={() => this._selectItemInEditMode()}
            onLongPress={() => this._onSelectItem()}>
          <EventTime event={event}/>
          <EventIcon event={event}/>
          <EventImage event={event} {...this.props}/>
        </TouchableOpacity>
      </View>
    );
  }

  _selectItemInEditMode(){
      const { event } = this.props;
      this.props.updateSelectedEventsInEditMode(event.id);
  }

  _onSelectItem(){
      const { event } = this.props;
      this.props.updateSelectedEvents(event.id);
  }
}

const styles = StyleSheet.create({
  rowContainer: {
    flex: 1,
    flexDirection: 'row', 
    paddingRight: 10,
  },
  container: {
    flex: 1,
  },
  highlighted: {
    backgroundColor: '#FA7B5F'
  },
  unhighlighted: {
    backgroundColor: 'transparent'
  }
});
