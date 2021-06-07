const { GroupHandler } = require("./Handler");

class ControllerGroupHandler extends GroupHandler {
    constructor() {
        super();
    }

    updateDoor(doorGroup) {
        this.group.forEach(g => {
            g.availableDoors(doorGroup);
        });
    }
}

module.exports = ControllerGroupHandler;