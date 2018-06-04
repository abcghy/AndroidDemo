const WebSocket = require('ws')

const wss = new WebSocket.Server({
    port: 8999
});

console.log("open in port: 8999")

wss.on('connection', function connection(ws, request) {
    console.log("open! ip: " + request.connection.remoteAddress)
    
    ws.send("Bravo! Open!")

    ws.on('message', function incoming(message) {
      console.log('received: %s', message);
    });

    var interval = setInterval(function() {
        ws.send('something');
    }, 5000);

    ws.on('close', function(code, reason) {
        clearInterval(interval)
        console.log(wss.clients.size)
        console.log("code: " + code + ", reason: " + reason)
    })
});