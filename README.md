# Getting Started

## Quick Start
To start the backend,
```
gradlew bootrun
```
## DevContainers
If you're lazy to install Java, you may run the project in VS Code using DevContainers extention with the given `Dockerfile`.


## Testing endpoints
### Streaming JSON response via HTTP GET calls
You may call the sample `/notifications` endpoint by,
```
curl --location 'http://localhost:8080/notifications' \
--header 'Content-Type: application/stream+json'
```

## RSocket
1. Download `rsc` from https://github.com/making/rsc.
2. Test the respective rSocket endpoints:
    ```
    // Request-response
   rsc --request --route=/request-response --data="my sample input" tcp://localhost:7000
   
   // Fire-forget
   rsc --request --route=/fire-forget --data="my sample input" tcp://localhost:7000 
       
    // Request-stream
   rsc --stream --route=/request-stream --data="my sample input"  tcp://localhost:7000
    ```



