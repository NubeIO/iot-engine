# Sandbox Environment

Overview on [Wiki](https://github.com/NubeIO/iot-engine/wiki/Dev-%7C-Docker)

## Prerequisite

Build docker images:

- `iot-engine`: `gradle clean jooq build docker`
- [dashboard](https://github.com/NubeIO/dashboard/tree/feature/dockerize-frontend#build-docker): `./docker.sh`

## Dashboard stack

```bash
./start.sh dashboard ditto
```

### Server information

- [keycloak](http://localhost:9000): admin/admin
- [mongo](localhost:27017): Port `27017`
  - DB Admin: `mongo/mongo`
  - Database: `sandbox` - `sandbox/111`
  - Database: `ditto` - `ditto/ditto`
- [dashboard](http://localhost:81): sandbox/111
- [dashboard-api](http://localhost:8080): sandbox/111
- [ditto](http://localhost:7000): sandbox/111

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

- Start `edge` stack in another command line windows. If you want to test `kafka`, simple append `kafka` in `start.sh` script

  ```bash
    ./start.sh edge kafka
  ```

- Then install `edge-kafka-producer` on `bios` via `connector-edge` with below payload. Refers [Postman](https://documenter.getpostman.com/view/670606/RWguwGk8#51b425c4-59be-4c13-b33c-e2db84830494)

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

## Cleanup your environment

- Remove `sandbox container`

```bash
docker rm -f $(docker ps -a | grep sandbox | awk '{print $1}')
```

- Remove `sandbox images`

```bash
docker rmi -f $(docker images | grep "nube\|iot" | awk '{print $3}')
```

- Remove `sandbox volume` - **Note**: careful, it will erase all your container data

```bash
docker volume rm $(docker volume ls | grep sandbox)
```
