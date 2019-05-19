import { createSlice } from 'redux-starter-kit';
import moment from 'moment';
import _ from 'lodash';

const thumbnailPath = 'http://www.sunsetteez.com/cms/includes/gallery/includes/gallery/samples/detailed-custom-embroidery-image.jpg';
const videoPath = 'rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov';
const testEvents = [
    {id: 1, thumbnailPath: thumbnailPath, videoPath: videoPath, date: Date.now(), message: 'PERSON_DETECTED_EVENT'},
    {id: 2, thumbnailPath: thumbnailPath, videoPath: videoPath, date: Date.now(), message: 'VEHICLE_DETECTED_EVENT'},
    {id: 3, thumbnailPath: thumbnailPath, videoPath: videoPath, date: Date.now(), message: 'ANIMAL_DETECTED_EVENT'},
]
const eventsSlice = createSlice({
    slice: 'events',

    initialState: {
        events: [],
        editMode: false
    },
      
    reducers: {
      loadEvents(state, action){
        let events = action.payload;
        state.events = events;
        //state.events = testEvents;
      },

      addEvents(state, action) {
          const newEvents = action.payload;
          if(newEvents){
            state.events.unshift(newEvents);
          }
      },

      toggleEventSelection(state, action) {
        const eventId = action.payload;
        const selectedEvent = state.events.find(event => event.id === eventId);
        selectedEvent.selected = !selectedEvent.selected;
        
        const isAnyEventSelected = state.events.some(event => event.selected === true);
        state.editMode = isAnyEventSelected;
      },

      deleteSelectedEvents (state, action) {
        const eventsAfterDelete = state.events.filter(event => event.selected !== true);
        state.events = eventsAfterDelete;
        state.editMode = false;
      }
    }
});

// Extract the action creators object and the reducer
const { actions, reducer } = eventsSlice;
// Extract and export each action creator by name
export const { loadEvents, addEvents, toggleEventSelection, deleteSelectedEvents } = actions;
// Export the reducer, either as a default or named export
export default reducer;


  