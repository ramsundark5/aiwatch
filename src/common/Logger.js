import bugsnag from './BugSnag';
class Logger{

    constructor(){
    }

    log(msg){
        //deviceLog.debug(msg);
        console.log(msg);
    }
    error(err){
        console.log(err);
        //deviceLog.error(err);
        bugsnag.notify(err);
    }
}
export default new Logger();