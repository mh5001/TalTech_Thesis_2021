const {SocketHandler} = require('./Handler');

class Controller extends SocketHandler {
    constructor(socket) {
        super(socket);
        socket['type'] = "CONTROLLER";
        this.doors = null;
        this.selectedWorker = null;
    }
    
    handleData(data) {
        const command = data[0];
        const argv = data.substr(1);

        if (command === 'b') {
            this.bindWithIp(argv);
        }
        if (command === 'a') {
            if (this.selectedWorker) this.selectedWorker.getDoorParameters(this);
        }
        if (command === 'v') {
            if (this.selectedWorker) this.selectedWorker.changeData(argv);
        }
    }

    bindWithIp(ip) {
        if (ip === ' ') this.selectedWorker = null;
        else {
            const doorSocket = this.doors.getDoor(ip);
            if (doorSocket) {
                this.selectedWorker = doorSocket;
                this.selectedWorker.requestGraphData(this);
                this.selectedWorker.getDoorParameters(this);
            }
        }
    }

    updateParameters(data) {
        this.writeData(data);
    }

    sendGraphData(graphData) {
        this.writeData(graphData);
    }

    availableDoors(doors) {
        this.doors = doors;
        let doorIps = this.doors.listData();
        const payload = 'i' + doorIps;

        this.writeData(payload);
    }
}

module.exports = Controller;