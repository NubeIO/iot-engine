# NubeIO IoT HOW-TO

## Run-Local

### Prerequisites

- Java 8
- [Gradle](https://gradle.org/)
- Create `nexus.secret.properties` with content:

  ```properties
  nexusSnapshotUrl=http://<your-nexus-server>/repository/maven-snapshots/
  nexusReleaseUrl=http://<your-nexus-server>/repository/maven-releases/
  nexusUsername=<your-nexus-user>
  nexusPassword=<your-nexus-password>
  ```

### Build

- Build local: compile and build to `jar` file, then push artifact to `local maven` repository.

  ```bash
  gradle clean build uberJar
  ```

- Publish artifacts to `Nexus` server:

  ```bash
  gradle publish
  ```

### Run Demo

- Copy theses artifacts to `demo` folder: (will be removed when I standardize build and bundle script)

  - `cp -rf dashboard/connector/edge/build/libs/nube-dashboard-connector-edge-1.0.0-SNAPSHOT-fat.jar demo/bios-connector.jar`
  - `cp -rf edge/bios/build/libs/nube-edge-bios-1.0.0-SNAPSHOT-fat.jar demo/bios.jar`
  - `cp -rf dashboard/server/build/libs/nube-dashboard-server-1.0.0-SNAPSHOT-fat.jar demo/dashboard.jar`

- Start services (replace `${your-ip}`)

  ```bash
  java -Dlogback.configurationFile=logback-bios-connector.xml -jar bios-connector.jar -conf bios-connector.json

  java -Dlogback.configurationFile=logback-bios.xml -jar bios.jar -conf bios.json

  java -Dlogback.configurationFile=logback-dashboard.xml -jar dashboard.jar -conf dashboard.json
  ```

## Interact REST API

Will be part of `Swagger UI` soon. Try [`Postman`](https://documenter.getpostman.com/view/670606/RWguwGk8) version first.

## Development

Start application in `IDE` for debug purpose

### Eclipse

- Install [`BuildShip`](https://projects.eclipse.org/projects/tools.buildship)
- Run:

	```bash
	gradle eclipse
	```

- Import Gradle project into Eclipse
- Use `launcher` script in same directory name to start application. The configuration is in `demo` folder
	- `BIOS.launch`: For `edge > bios`
	- `BIOS-Connector.launch`: For `dashboard > connector > edge`
	- `Dashboard.launch`: For `dashboard > server`


### IntelliJ

TBD

## Deployment

TBD
