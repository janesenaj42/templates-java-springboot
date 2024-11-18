# Getting Started

## Quick Start
To start the backend,
```
gradlew bootrun
```

You may call the sample `/notifications` endpoint by,
```
curl --location 'http://localhost:8080/notifications' \
--header 'Content-Type: application/stream+json'
```

### DevContainers
If you're lazy to install Java, you may run the project in VS Code using DevContainers extention with the given `Dockerfile`.



