# FacadeServer API documentation

FacadeServer exposes access to the various resources both with an HTTP REST API and over WebSocket.

### Specification
The API specification is defined in the [OpenAPI 3.0](https://github.com/OAI/OpenAPI-Specification) format, and is contained in the `swagger.json` file at the root of this repository.
You can view it in a browser at [this link](http://petstore.swagger.io/?url=https://raw.githubusercontent.com/gianluca-nitti/FacadeServer/rest-methods/src/main/resources/web/swagger.json#/).

### HTTP
All the information necessary to interact with FacadeServer over HTTP is explained in the previously mentioned specification;
the base path for the HTTP API is `/api`, so to request, for example, the server Message Of The Day, you need to do a `GET /api/resources/config/MOTD`.

### WebSocket
The WebSocket protocol allows to access the same resources provided over HTTP, with the plus of supporting push notifications.
WebSocket connections are accepted at the `/ws` endpoint.

It is defined as follows:
* A message sent from the client to the server is a JSON object with two keys, `messageType` and `data`.
    `messageType` is an enum, and the following values are allowed: `RESOURCE_REQUEST`, `AUTHENTICATION_REQUEST` and `AUTHENTICATION_DATA`.
    * If `messageType` is `RESOURCE_REQUEST`, the message is a request to one of the resources that are accessible via HTTP under `/resources`. `data` must be an object with these keys:
       * `method`: The request method to the resource; allowed values are `GET`, `POST`, `PUT`, `PATCH` and `DELETE`.
       * `resourcePath`: The path to the resource to request, as a JSON array. For example, setting it to `["games", "myGame"]` points the request to the same resource which can be accessed via HTTP at `/api/resources/games/myGame`.
       * `data` (optional): The request payload data. Usually, must not be sent with GET requests; refer to the specification for detailed information.
    
        Here is an example message to request the execution of the `say hello, world` console command:
        ```
        {
            "messageType": "RESOURCE_REQUEST",
            "data": {
                "method": "POST",
                "resourcePath": ["console"],
                "data": "say hello, world"
            }
        }
        ```
     * If `messageType` is `AUTHENTICATION_REQUEST`, no `data` must be specified.
        With the `{"messageType": "AUTHENTICATION_REQUEST"}` message, the client indicates to the server that it no longer wants to send requests anonymously, and wants to initiate an authentication handshake.
        The server will then answer with a server handshake hello message (see below for more details).
     * A message with `messageType` set to `AUTHENTICATION_DATA` must be sent by the client after it has received the server handshake hello message and has generated the authentication data using the said message and the client identity certificate.
        For more detail on how to generate the authentication data, which must be sent as the `data` key to finish the authentication handshake, please consult the "POST /auth" section of the API specification.
* A message sent from the server to the client is a JSON object with the keys `messageType`, `resourcePath` and `data`.
The possible values of `resourcePath` are `ACTION_RESULT`, `RESOURCE_CHANGED` and `RESOURCE_EVENT`.
    * If `messageType` is `ACTION_RESULT`, the server is either sending the authentication handshake hello or the response to a client request.
       In both cases, `data` is an object with the following keys:
       * `status`: enum which carries information about the response status, like HTTP response status codes. Possible values are `OK`, `BAD_REQUEST`, `FORBIDDEN`, `ACTION_NOT_ALLOWED`, `NOT_FOUND`, `GENERIC_ERROR`, `CONFLICT`;
       * `message`: if `status` is not `OK`, contains an optional error message;
       * `data`: contains the response data, if any, or the authentication handshake data.
       In the second case only (the message is a response to a request), in the root object the `resourcePath` key is set to the path of the resource which is answering the request, in the array format (as described above).
    * If `messageType` is `RESOURCE_CHANGED`, the server is notifying the client that the data in a resource has changed.
        `resourcePath` is set to the path of the resource which has changed, and `data` contains its new value, the same that could be obtained by performing a GET request to the resource.
    * If `messageType` is `RESOURCE_EVENT`, the server is notified that a resource has emitted an event.
        As before, `resourcePath` is set to the path of the resource which has generated the event.
        `data` carries the serialized event data.
        At the moment, the only resource which emits events is the console, and a console event is an object with the `messageType` (can be `CONSOLE`, `CHAT`, `ERROR` or `NOTIFICATION`) and `message` (the actual message text) keys.
        
        Via HTTP, events for a client are put in a queue on the server and accessible at the `GET /events` endpoint (which also drains the queue - see the specification for more detail).