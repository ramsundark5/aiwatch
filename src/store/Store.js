import { configureStore, getDefaultMiddleware } from 'redux-starter-kit';
import logger from 'redux-logger';
import { combineReducers } from 'redux';
import cameraReducer from './CamerasStore';
import eventReducer from './EventsStore';
import settingsReducer from './SettingsStore';

const rootReducer = combineReducers({
    cameras: cameraReducer,
    events: eventReducer,
    settings: settingsReducer
});

export const store = configureStore({
    reducer: rootReducer,
    middleware: [logger, ...getDefaultMiddleware()],
});
