# Getting Started

## Quick Start
To start the backend,
```
gradlew bootrun
```

### Setup
This project uses keycloak for authentication. To set up keycloak, run
```declarative
docker compose up -d
```
Keycloak will be available at http://localhost:8080

(TODO: include keycloak setup guide)

### DevContainers
If you're lazy to install Java, you may run the project in VS Code using DevContainers extention with the given `Dockerfile`.


## Testing endpoints
### Streaming JSON response via HTTP GET calls
You may call the sample `/notifications` endpoint by,
```
curl --location 'http://localhost:8080/notifications' \
--header 'Content-Type: application/stream+json'
```

### RSocket
1. Download `rsc` from https://github.com/making/rsc.
2. RSocket endpoints requires authencation. Get the bearer token using the following curl command, updating `KEYCLOAK_URL`, `REALM_NAME`, `CLIENT_ID`, `USERNAME` and `PASSWORD` as required.
   ```
   export KEYCLOAK_URL=http://localhost:8080
   export REALM_NAME=my-realm
   export CLIENT_ID=my-sample-app
   export USERNAME=asdf
   export PASSWORD=asdfasdf
   
   ACCESS_TOKEN=$(curl -s --location "$KEYCLOAK_URL/realms/$REALM_NAME/protocol/openid-connect/token" \
   --header "Content-Type: application/x-www-form-urlencoded" \
   --data-urlencode "grant_type=password" \
   --data-urlencode "client_id=$CLIENT_ID" \
   --data-urlencode "username=$USERNAME" \
   --data-urlencode "password=$PASSWORD" \
    | grep -o '"access_token":"[^"]*"' | cut -d':' -f2 | tr -d '"' ) && echo $ACCESS_TOKEN
   ``` 
3. Test the respective rSocket endpoints:
    ```
    // Request-response
   rsc --request --route=/request-response --authBearer=$ACCESS_TOKEN --data="my sample input" tcp://localhost:7000
   
   // Fire-forget
   rsc --request --route=/fire-forget --authBearer=$ACCESS_TOKEN --data="my sample input" tcp://localhost:7000 
       
    // Request-stream
   rsc --stream --route=/request-stream --authBearer=$ACCESS_TOKEN --data="my sample input"  tcp://localhost:7000
    ```
   



