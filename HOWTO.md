# NubeIO IoT HOW-TO

## Run-Local

### Prequisitance

- Java 8
- [Maven](https://maven.apache.org/)
- Clone `settings.xml.template` to `settings-<your-name>.xml`, and update xml variable properties with prefix `your-`.
  - `${your-nexus-server}`: `Nexus` server url
  - `${your-nexus-user}`: `Nexus` username
  - `${your-nexus-password}`: `Nexus` password
- Clone `cluster.xml.template` to `cluster-<your-name>.xml`, and update xml variable properties with prefix `your-`.
  - `${your-network-interface}`: Your network interface that allow access internet. For example: `192.168.1.5` is your computer IP, gateway almost is `192.168.1.1`, so you can set network interface is `192.168.1.*`

### Build

Only run 1 of 2 commands:

- Build local: compile and build to `jar` file, then push artifact to `local maven` repository.

  ```bash
  mvn clean install -s settings-<your-name>.xml
  ```

- Share artifacts to `Nexus` server: it contains `build` step above and push to `remote maven` repository.

  ```bash
  mvn clean deploy -s settings-<your-name>.xml
  ```

### Run

- Copy theses artifacts to `build` (or whatever) folder: (will be removed when I standardize build and bundle script)
  - `cp -rf nube-bios/target/nube-bios-1.0-SNAPSHOT-fat.jar build/`
  - `cp -rf nube-bios/src/conf/config.json build/bios-config.json`
  - `cp -rf nube-app-store-rest/target/nube-app-store-rest-1.0-SNAPSHOT-fat.jar build/`
  - `cp -rf nube-app-store-rest/src/conf/config.json build/appstore-config.json`
  - `cp -rf cluster-<your-name>.xml logback.xml build/`
- Edit `build/bios-config.json` with `remotes` as your nexus server

  ```json
  "remotes": [
    "http://${your-nexus-server}/repository/maven-releases/",
    "http://${your-nexus-server}/repository/maven-snapshots/",
    "http://${your-nexus-server}/repository/maven-central/"
  ]
  ```

- Edit `build/appstore-config.json` with desired port

  ```json
  {
    "http.port": 8086,
    "api.name": "app-store"
  }
  ```

- Start services (replace `cluster-${your-name}.xml` and `${your-ip}`)

  ```bash
  java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory -Dvertx.hazelcast.config=cluster-${your-name}.xml -Dhazelcast.logging.type=slf4j -jar nube-app-store-rest-1.0-SNAPSHOT-fat.jar -conf appstore-config.json -cluster -cluster-host ${your-ip}
  ```

  ```bash
  java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory -Dvertx.hazelcast.config=cluster-${your-name}.xml -Dhazelcast.logging.type=slf4j -jar nube-bios-1.0-SNAPSHOT-fat.jar -conf bios-config.json -cluster -cluster-host ${your-ip}
  ```

## Interact REST API

Will be part of `Swagger UI` soon

- Node status

  - `GET::localhost:8086/nodes`

- Module manipulates

  - `POST::localhost:8086/api/module/`

    Request

    ```json
    {
      "groupId": "io.nubespark",
      "artifactId": "nube-edge-ditto-driver",
      "version": "1.0-SNAPSHOT"
    }
    ```

  - `DELETE::localhost:8086/api/module/`

    Request

    ```json
    {
      "artifactId": "nube-edge-ditto-driver"
    }
    ```

  - `PUT::localhost:8086/api/module/`

## Development

### Prequisitance

- Same as [Prequisitance](#Run-Local#Prequisitance)
- IDE: Eclipse or IntelliJ

## Deployment

TBD
