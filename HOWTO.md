# NubeIO IoT HOW-TO

## Run-Local

### Prerequisites

- Java 8
- [Gradle](https://gradle.org/)
- Create `nexus.secret.properties` with content:

  ```properties
  nexusSnapshotUrl=http://<your-nexus-server>/repository/maven-snapshots/
  nexusReleaseURL=http://<your-nexus-server>/repository/maven-releases/
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

- Start services (replace `${your-ip}`)

  ```bash
  java -Dlogback.configurationFile=logback-bios-connector.xml -jar bios-connector.jar -conf bios-connector.json -cluster

  java -Dlogback.configurationFile=logback-bios.xml -jar bios.jar -conf bios.json -cluster
  ```

## Interact REST API

Will be part of `Swagger UI` soon. Try [`Postman`](https://web.postman.co/collections/670606-552e7446-ec7e-496f-a437-541f782583a1?workspace=48db9612-bb94-4180-8ae6-cfdde440c9a9#8815e2c8-feab-4012-9a5e-48a17e6f9159) version first.

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
