# Requirements
- JDK 16

# Configuration
see resources/application.properties
- coindesk.url=https://api.coindesk.com/v1/bpi

# Build
### Build and run all tests
```
./gradlew[.bat] clean build
```

### Build fat jar
```
./gradlew[.bat] clean shadowJar
```

### Build docker image
```
docker build -t user/bitcoin .
```

### Run docker image
```
docker run -it user/bitcoin .
```