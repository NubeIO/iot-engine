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
  - `cp -rf nube-app-store-rest/target/nube-app-store-rest-1.0-SNAPSHOT-fat.jar build/`
  - All `*.template` files and remember to edit any variables start with `${your-`

- Start services (replace `${your-ip}`)

  ```bash
  java -Dlogback.configurationFile=logback-rest.xml -jar nube-app-store-rest-1.0-SNAPSHOT-fat.jar -conf rest-config.json -cluster -cluster-host ${your-ip}
  ```

  ```bash
  java -Dlogback.configurationFile=logback.xml -jar nube-bios-1.0-SNAPSHOT-fat.jar -conf bios-config.json -cluster -cluster-host ${your-ip}
  ```

## Interact REST API

Will be part of `Swagger UI` soon.

## Development

### Prequisitance

- Same as [Prequisitance](#Run-Local#Prequisitance)
- IDE: Eclipse or IntelliJ

## Deployment

TBD
