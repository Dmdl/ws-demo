const {
    RSocketClient,
    JsonSerializer,
    IdentitySerializer
} = require('rsocket-core');
const RSocketWebSocketClient = require('rsocket-websocket-client').default;
let client = undefined;

function addErrorMessage(prefix, error) {
    let ul = document.getElementById("messages");
    let li = document.createElement("li");
    li.appendChild(document.createTextNode(prefix + error));
    ul.appendChild(li);
}

function addMessage(message) {
    let ul = document.getElementById("messages");

    let li = document.createElement("li");
    li.appendChild(document.createTextNode(JSON.stringify(message)));
    ul.appendChild(li);
}

function main() {
    if (client !== undefined) {
        client.close();
        document.getElementById("messages").innerHTML = "";
    }

    // Create an instance of a client
    client = new RSocketClient({
        // serializers: {
        //     data: JsonSerializer,
        //     metadata: IdentitySerializer
        // },
        setup: {
            // format of `data`
            // dataMimeType: 'application/json',
            dataMimeType: 'text/plain',
            // ms btw sending keepalive to server
            keepAlive: 60000,
            // ms timeout if no keepalive response
            lifetime: 180000,
            // format of `metadata`
            // metadataMimeType: 'message/x.rsocket.routing.v0',
            metadataMimeType: 'text/plain',
        },
        transport: new RSocketWebSocketClient({
            url: 'ws://localhost:7000'
        }),
    });

    // Open the connection
    client.connect().subscribe({
        onComplete: socket => {
            // socket provides the rsocket interactions fire/forget, request/response,
            // request/stream, etc as well as methods to close the socket.
            socket.requestStream({
                // data: {
                //     'author': document.getElementById("author-filter").value
                // },
                // metadata: String.fromCharCode('tweets.by.author'.length) + 'tweets.by.author',
                data: ">> to server",
                metadata: String.fromCharCode("request.stream".length) + "request.stream"
            }).subscribe({
                onComplete: () => console.log('complete'),
                onError: error => {
                    console.log(error);
                    addErrorMessage("Connection has been closed due to ", error);
                },
                onNext: payload => {
                    console.log(payload);
                    addMessage(payload.data);
                },
                onSubscribe: subscription => {
                    subscription.request(2147483647);
                },
            });
        },
        onError: error => {
            console.log(error);
            addErrorMessage("Connection has been refused due to ", error);
        },
        onSubscribe: cancel => {
            /* call cancel() to abort */
        }
    });
}

document.addEventListener('DOMContentLoaded', main);
document.getElementById('author-filter').addEventListener('change', main);