import bugsnag from './BugSnag';

class Logger{
    log(obj){
        console.log(log);
        bugsnag.notify(obj);
    }
}

export default new Logger();