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
        this.init();
    }

    init() {
        console.log = this.interceptLog(console.log);
        console.info = this.interceptLog(console.info);
        console.error = this.interceptLog(console.error);
        //console.debug = interceptLog(console.debug);
    }
    
    interceptLog(originalFn) {
      return function() {
          try{
            const args = Array.prototype.slice.apply(arguments);
            let result = '';
            for (let i = 0; i < args.length; i++) {
                const arg = args[i];
                if (!arg || (typeof arg === 'string') || (typeof arg === 'number')) {
                    result += arg;
                }
                else {
                    result += JSON.stringify(arg);
                }
            }
            //originalFn.call(console, 'INTERCEPTED LOG: ' + result);
            this.log(result);
          }catch(err){
            //swallow the exception
          }
          return originalFn.apply(console, arguments);
      };
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