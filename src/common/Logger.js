import bugsnag from './BugSnag';

class Logger{
    log(msg){
        console.log(msg);
    }
    error(err){
        console.log(err);
        bugsnag.notify(err);
    }
}

export default new Logger();