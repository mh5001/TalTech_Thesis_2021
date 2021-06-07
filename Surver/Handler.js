class SocketHandler {
    constructor(socket) {
        this.socket = socket;
        this.socket.on('data', data => {
            this.handleData(data.toString('utf-8'));
        });
    }

    handleData(data) {}
    // Simple wrapper for TCP messaging, first 4 bytes are the size of the package
    writeData(data) {
        let size = data.length;
        while (size.toString().length < 4) size = "0" + size;

        this.socket.write(Buffer.from(`${size}${data}`));
    }

    isWrapper(socket) {
        return this.socket['uuid'] === socket['uuid'];
    }
}

class GroupHandler {
    constructor() {
        this.group = [];
    }

    add(socketHandler) {
        socketHandler.socket['uuid'] = this.group.length;
        this.group.push(socketHandler);
    }

    remove(socket) {
        for (let i = 0; i < this.group.length; i++) {
            if (this.group[i].isWrapper(socket)) {
                this.group.splice(i, 1);
                break;
            }
        }
    }
}

module.exports = {
    SocketHandler: SocketHandler,
    GroupHandler: GroupHandler
};