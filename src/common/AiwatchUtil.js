class AiwatchUtil{
    uuidv4() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
          var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
          return v.toString(16);
        });
    }

    asyncSetState = instance => newState => new Promise(resolve => instance.setState(newState, resolve));

    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    }
}

export default new AiwatchUtil();