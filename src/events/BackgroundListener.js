import { NativeEventEmitter } from 'react-native';
import { store } from '../store/Store';
import { addEvents } from '../store/EventsStore';
import { editCamera } from '../store/CamerasStore';
import { updateSettings } from '../store/SettingsStore';
import RNSmartCam from '../native/RNSmartCam';
import Logger from '../common/Logger';
class BackgroundListener{

    init(){
        const DETECTION_EVENT = new NativeEventEmitter(RNSmartCam);
        DETECTION_EVENT.addListener(
            'NEW_DETECTION_JS_EVENT',
            this.handleNewDetectionEvent
        );

        DETECTION_EVENT.addListener(
            'STATUS_CHANGED_EVENT_JS_EVENT',
            this.handleCameraStatusChange
        );

        this.loadSettings();
    }

    handleNewDetectionEvent = (event) => {
        store.dispatch(addEvents(event));
    }

    handleCameraStatusChange = (event) => {
        store.dispatch(editCamera(event));
    }

    async loadSettings(){
        try{
            let settings = await RNSmartCam.getSettings();
            store.dispatch(updateSettings(settings));
        }catch(err){
            Logger.log(err);
        }
    }
}

export default new BackgroundListener();