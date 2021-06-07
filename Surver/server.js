const net = require('net');
const ControllerGroupHandler = require('./ControllerGroupHandler');
const DoorGroupHandler = require('./DoorGroupHandler');
const Controller = require('./Controller');
const Door = require('./Door');

const PORT = 38368;
// Group of doors are wrapped in a class for cleaner code
const doorGroup = new DoorGroupHandler();
const controllerGroup = new ControllerGroupHandler();

// Creating TCP Server
const server = net.createServer(socket => {
    socket.setKeepAlive(true);
    socket.once('data', data => {
        const msg = data.toString('utf-8');
        // These checks the role of the socket
        if (msg === 'amController') {
            console.log("New Controller");
            const c = new Controller(socket);
            controllerGroup.add(c);
            // Update the available doors in the system
            c.availableDoors(doorGroup);
        }
        if (msg === 'amWorker') {
            const w = new Door(socket, () => {
                // This function is called after everything initiated (server received the IP of the door)
                doorGroup.add(w);
    
                // Update available doors again, but crash somehow
                controllerGroup.updateDoor(doorGroup);
            });
        }
    });
    // Removing available door from system when the socket is closed
    socket.once('error', () => {
        handleSocketClose(socket);
    });
    socket.once('close', () => {
        handleSocketClose(socket);
    });
});

function handleSocketClose(socket) {
    if (socket['type'] === 'WORKER') {
        doorGroup.remove(socket);
        controllerGroup.updateDoor(doorGroup);
    }
    if (socket['type'] === 'CONTROLLER') {
        controllerGroup.remove(socket);
    }
}

server.listen(PORT, () => {
    console.log("Server is ready");
});