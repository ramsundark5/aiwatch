import { NativeEventEmitter } from 'react-native';
import { store } from '../store/Store';
import { addEvents } from '../store/EventsStore';
import { updateStatus } from '../store/CamerasStore';
import RNSmartCam from '../native/RNSmartCam';
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
    }

    handleNewDetectionEvent = (event) => {
        store.dispatch(addEvents(event));
    }

    handleCameraStatusChange = (event) => {
        store.dispatch(updateStatus(event));
    }
}

export default new BackgroundListener();