const {GroupHandler} = require('./Handler');

class DoorGroupHandler extends GroupHandler {
    constructor() {
        super();
    }

    listData() {
        let output = "";
        this.group.forEach(g => {
            output += g.getIp() + ',';
        });
        output = output.substring(0, output.length - 1);
        
        return output;
    }

    getDoor(ip) {
        for (let i = 0; i < this.group.length; i++) {
            if (this.group[i].getIp() === ip) {
                return this.group[i];
            }
        }
    }
}

module.exports = DoorGroupHandler;