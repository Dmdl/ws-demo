// const {RSocketClient} = require('rsocket-core');
// const RSocketWebsocketClient = require('rsocket-websocket-client').default;
// const WebSocket = require('ws');
const {Flowable} = require('rsocket-flowable');
//const logger = require('./lib/logger');
const {
    RSocketClient,
    JsonSerializer,
    IdentitySerializer
} = require('rsocket-core');
const RSocketWebSocketClient = require('rsocket-websocket-client').default;
let client = undefined;

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

let requestsSink;

const requestsSource = new Flowable((requestsSubscriber) => {
    // Number of the requests requested by subscriber.
    let requestedRequests = 0;
    // Buffer for requests which should be sent but not requested yet.
    const pendingRequests = [];
    let completed = false;

    requestsSink = {
        sendRequest(myRequest) {
            if (completed) {
                // It's completed, nobody expects this request.
                return;
            }
            if (requestedRequests > 0) {
                --requestedRequests;
                requestsSubscriber.onNext(myRequest);
            } else {
                pendingRequests.push(myRequest);
            }
        },
        complete() {
            if (!completed) {
                completed = true;
                requestsSubscriber.onComplete();
            }
        },
    };

    requestsSubscriber.onSubscribe({
        cancel: () => {
            // TODO: Should be handled somehow.
        },
        request(n) {
            const toSend = pendingRequests.splice(n);
            requestedRequests += n - toSend.length;
            for (const pending of toSend) {
                requestsSubscriber.onNext(pending);
            }
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
            socket.requestChannel(requestsSource.map(item => item)).subscribe({
                onComplete: () => console.log('complete'),
                onError: error => {
                    console.log(error);
                },
                onNext: payload => {
                    console.log('>>>> ' + JSON.stringify(payload));
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
    //let chatMessage = document.getElementById("message").value;
    requestsSink.sendRequest({data: 'new message'});
}

document.addEventListener('DOMContentLoaded', main);