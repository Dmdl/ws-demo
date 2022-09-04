// const {RSocketClient} = require('rsocket-core');
// const RSocketWebsocketClient = require('rsocket-websocket-client').default;
// const WebSocket = require('ws');
const {Flowable} = require('rsocket-flowable');
const logger = require('./lib/logger');
const {
    RSocketClient,
    JsonSerializer,
    IdentitySerializer
} = require('rsocket-core');
const RSocketWebSocketClient = require('rsocket-websocket-client').default;
let client = undefined;

// const transportOptions = {
//     url: 'ws://localhost:7001',
//     wsCreator: (url) => {
//         return new WebSocket(url);
//     }
// };
//
// const setup = {
//     keepAlive: 60000,
//     lifetime: 180000,
//     dataMimeType: 'text/plain',
//     metadataMimeType: 'text/plain'
// };
//
// const transport = new RSocketWebsocketClient(transportOptions);
// const client = new RSocketClient({setup, transport});

// function main() {
//     client.connect().subscribe({
//         onComplete: (socket) => {
//             logger.info('Client connected to the RSocket server');
//
//             let clientRequests = ['a', 'b', 'c'];
//
//             clientRequests = clientRequests.map((req) => {
//                 return {
//                     data: req
//                 };
//             });
//
//             let subscription;
//
//             const stream = Flowable.just(...clientRequests);
//
//             socket.requestChannel(stream).subscribe({
//                 onSubscribe: (sub) => {
//                     subscription = sub;
//                     logger.info(`Client is establishing a channel`);
//                     subscription.request(0x7fffffff);
//                 },
//                 onNext: (response) => {
//                     logger.info(`Client recieved: ${JSON.stringify(response)}`);
//                 },
//                 onComplete: () => {
//                     logger.info(`Client received end of server stream`);
//                 }
//             });
//         }
//     });
// }

function addMessage(data) {
    let div = document.getElementById("comments");

    let avatar = document.createElement("img");
    avatar.setAttribute("src", "avatar.png");
    avatar.setAttribute("class", "avatar");

    let innerDiv = document.createElement("div");
    innerDiv.appendChild(avatar);
    innerDiv.appendChild(document.createTextNode(data));
    innerDiv.setAttribute('class', 'msg-receive');
    div.appendChild(innerDiv);
}

const flowable = new Flowable(subscriber => {
    subscriber.onSubscribe({
        request: msg => {
            subscriber.onNext(msg);
        }
    });
});

function main() {
    if (client !== undefined) {
        client.close();
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
            url: 'ws://localhost:7001'
        }),
    });

    // Open the connection
    client.connect().subscribe({
        onComplete: socket => {
            let clientRequests = ['a', 'b', 'c'];
            clientRequests = clientRequests.map((req) => {
                return {
                    data: req
                };
            });
            const stream = Flowable.just(...clientRequests);
            socket.requestChannel(stream).subscribe({
                onComplete: () => console.log('complete'),
                onError: error => {
                    console.log(error);
                },
                onNext: payload => {
                    logger.info(JSON.stringify(payload));
                    addMessage(payload.data);
                },
                onSubscribe: subscription => {
                    subscription.request(2147483647);
                },
            });
        },
        onError: error => {
            logger.error(error);
        },
        onSubscribe: cancel => {
            /* call cancel() to abort */
        }
    });
}

document.getElementById("submit").addEventListener("click", onClick);

function onClick() {
    let chatMessage = document.getElementById("message").value;
    flowable.subscribe({
        onComplete: () => console.log('done'),
        onError: (error) => console.error(error),
        onNext: (value) => console.log(value),
        // Nothing happens until `request(n)` is called
        onSubscribe: (sub) => sub.request(1),
    });
    flowable.subscribe()
}

document.addEventListener('DOMContentLoaded', main);