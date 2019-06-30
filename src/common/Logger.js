import bugsnag from './BugSnag';
import  { AsyncStorage } from 'react-native';
import deviceLog, {InMemoryAdapter} from 'react-native-device-log';
class Logger{

    constructor(){
        deviceLog.init(AsyncStorage /* You can send new InMemoryAdapter() if you do not want to persist here*/
        ,{
            //Options (all optional):
            logToConsole : true, //Send logs to console as well as device-log
            logRNErrors : true, // Will pick up RN-errors and send them to the device log
            maxNumberToRender : 2000, // 0 or undefined == unlimited
            maxNumberToPersist : 2000 // 0 or undefined == unlimited
        });
        //this.init();
    }

    log(msg){
        deviceLog.debug(msg);
        //console.log(msg);
    }
    error(err){
        //console.log(err);
        deviceLog.error(err);
        bugsnag.notify(err);
    }
}
export default new Logger();