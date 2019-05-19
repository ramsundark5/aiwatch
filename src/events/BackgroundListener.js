import { NativeEventEmitter } from 'react-native';
import { store } from '../store/Store';
import { addEvents } from '../store/EventsStore';
import RNSmartCam from '../native/RNSmartCam';

class BackgroundListener{

    init(){
        const DETECTION_EVENT = new NativeEventEmitter(RNSmartCam);
        DETECTION_EVENT.addListener(
            'NEW_DETECTION_JS_EVENT',
            this.handleNewDetectionEvent
          );
    }

    handleNewDetectionEvent = (event) => {
        //addEvents(event);
        store.dispatch(addEvents(event));
        //store.dispatch(ThreadActions.setCurrentThread(threadForContact));
    }
}

export default new BackgroundListener();