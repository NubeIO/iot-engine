# Sandbox Environment

Overview on [Wiki](https://github.com/NubeIO/iot-engine/wiki/Dev-%7C-Docker)

## Prerequisite

Build docker images:

- `iot-engine`: `gradle clean jooq build docker`
- [dashboard](https://github.com/NubeIO/dashboard/tree/feature/dockerize-frontend#build-docker): `./docker.sh`

## Dashboard stack

```bash
./start.sh dashboard
```

### Server information

- [keycloak](http://localhost:9000): admin/admin
- [mongo](localhost:27017): Port `27017` - mongo/mongo -  Database: sandbox
- [dashboard](http://localhost:81): sandbox/111
- [dashboard-api](http://localhost:8080): sandbox/111

## Edge stack

- Start `nexus` server

  ```bash
    ./start.sh nexus
  ```

- Refer [create nexus secret](../HOWTO.md#Prerequisites) with [nexus information](#edge-server-information)
- Publish artifact to `Nexus`

  ```bash
  gradle publish
  ```

- Start `edge` stack

  ```bash
    ./start.sh edge
  ```

- Then install `edge-kafka-producer` on `bios` via `connector-edge` with below payload. Refers [Postman](https://documenter.getpostman.com/view/670606/RWguwGk8#ed6d4b9b-2ffc-4ca7-99d2-2973c28c3af4)

  ```json
    {
        "group_id": "com.nubeiot.edge.connector.sample",
        "artifact_id": "kafka",
        "service_name": "edge-kafka-demo",
        "version": "1.0.0-SNAPSHOT",
        "deploy_config": {
            "__kafka__": {
                "__client__": {
                    "bootstrap.servers": [
                        "kafka:9092"
                    ]
                },
                "__security__": {
                    "security.protocol": "PLAINTEXT"
                }
            }
        }
    }
  ```

### Edge server information

- [connector-edge](http://localhost:8180): `BIOS` REST API connector
- [connector-kafka](http://localhost:8280/sample/index.html): dashboard kafka demo
- [nexus](http://localhost:8081): Nexus server - admin/admin123
- [kafka](http://localhost:9092): Kafka server
