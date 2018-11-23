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
  mvn clean install
  ```

- Share artifacts to `Nexus` server: it contains `build` step above and push to `remote maven` repository.

  ```bash
  mvn clean deploy -s settings-<your-name>.xml
  ```

### Run Demo

- Copy theses artifacts to `demo` folder: (will be removed when I standardize build and bundle script)

  - `cp -rf dashboard-connector-edge/target/edge-1.0.0-SNAPSHOT-fat.jar demo/`
  - `cp -rf edge-bios/target/bios-1.0.0-SNAPSHOT-fat.jar demo/`
  - `cp -rf conf/cluster.xml.template demo/cluster.xml` files and remember to update any variables with placeholder `${your-`

- Start services (replace `${your-ip}`)

  ```bash
  java -Dlogback.configurationFile=logback-rest.xml -jar edge-1.0.0-SNAPSHOT-fat.jar -conf rest-config.json -cluster -cluster-host ${your-ip}

  java -Dlogback.configurationFile=logback.xml -jar bios-1.0.0-SNAPSHOT-fat.jar -conf bios-config.json -cluster -cluster-host ${your-ip}
  ```

## Interact REST API

Will be part of `Swagger UI` soon. Try [`Postman`](https://web.postman.co/collections/670606-552e7446-ec7e-496f-a437-541f782583a1?workspace=48db9612-bb94-4180-8ae6-cfdde440c9a9#8815e2c8-feab-4012-9a5e-48a17e6f9159) version first.

## Development

### Prequisitance

- Same as [Prequisitance](#Run-Local#Prequisitance)
- IDE: Eclipse or IntelliJ

## Deployment

TBD
