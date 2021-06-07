const {SocketHandler} = require('./Handler');

class Door extends SocketHandler {
    constructor(socket, initiatedFunction) {
        super(socket);
        socket['type'] = "WORKER";
        socket.setKeepAlive();
        this.initiatedFunction = initiatedFunction;
        this._requestedController = null;
    }

    getIp() {
        return this.socket['localIp'];
    }
    
    handleData(data) {
        if (data[0] === 'm') {
            this.socket['localIp'] = data.substring(1);
            this.initiatedFunction();
            console.log('New worker: ' + this.socket['localIp']);
        }
        if (data[0] === 'u') {
            if (this._requestedController) this._requestedController.sendGraphData(data);
        }
        if(data[0] === 'A') {
            if (this._requestedController) this._requestedController.updateParameters(data);
        }
    }

    requestGraphData(controller) {
        this.writeData("r");
        this._requestedController = controller;
    }

    getDoorParameters(controller) {
        this.writeData("a");
        this._requestedController = controller;
    }

    changeData(argv) {
        let data = argv.split('|');
        let force = data[0];
        let time = data[1];
        if (force.length < 2) force = "0" + force;
        if (time.length < 2) time = "0" + time;
        this.writeData(`V${force}${time}`);
    }
}

module.exports = Door;