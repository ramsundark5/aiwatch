import { configureStore, getDefaultMiddleware } from 'redux-starter-kit';
import logger from 'redux-logger';
import { combineReducers } from 'redux';
import cameraReducer from './CamerasStore';
import eventReducer from './EventsStore';

const rootReducer = combineReducers({
    cameras: cameraReducer,
    events: eventReducer
});

export const store = configureStore({
    reducer: rootReducer,
    middleware: [logger, ...getDefaultMiddleware()],
});
