# NubeIO IoT Platform

IoT Platform provides a distributed system based on micro service that helps to:

- Lightweight and reliable deployments on hundred `edge device` remotely, while your devices remain online.
- Collect and store IoT data sensors by realtime in time-series database.
- Data Analytics and Data Visualization.

## Architecture

Use `vert.x` framework for building reactive applications with modularzation, non-blocking, small footprint.

### IoT Deployment

- App developers use Maven to write apps and install apps (jars) to central maven repo.

- A Nube App Store in cloud is responsible to send commands of install/update/uninstall, manage the jars, provide statistics and analytics about usage, etc.

- A Nube App Installer in edge devices is responsible to install/update/uninstall according to Nube App Store’s command and report status for statistics.

Following is the rough architecture.

![nube-vertx-engine architecture](docs/nube-app-store.png?raw=true "Architecture of Nube vert.x Engine")

#### nube-vertx-engine

- Parent project that will be used by developers to build modules.
- It has some configurations and conventions to be used by sub-modules.

#### nube-vertx-common

- Common code that will be used across all modules (Eg: Abstract Class, Parent Class, Cluster Config).
- It can be used as dependency by sub modules.

#### nube-app-installer

- Install/Uninstall/Update apps/modules by managing dependency from central maven repository.
- Report usage and statistics

#### nube-app-store-rest

- Nube App Store REST API for remotely installing/uninstalling/updating apps/modules.
- Nube App Store REST API to upgrade Nube App Installer in remote instance.
- Statistics / Analytics of used apps (TODO).
- Tracking and managing connected remote instances (TODO).

#### nube-bios

- Nube BIOS with Java 8 should be run on remote edge instance/device with clustering setting.
- Installs default version of Nube App Installer on startup using maven.
- Handles upgradation of Nube App Installer.

### IoT Dashboard

- Developed by developers according to business logic and requirement.
- Deployed in central maven repository after development and testing.

#### nube-vertx-hive

- A module for connecting FiloDB server and executing SQL operations on it.
- We have given a sql-hive/engine POST service available for sending SQL command.

#### nube-vertx-postgresql

- A module for connecting PostgreSQL server and executing SQL operations on it.
- We have given a sql-pg/engine POST service available for sending SQL command.

#### nube-vertx-mongo

- It makes the services available of MongoDB as the REST request.

#### nube-vertx-zeppelin

- Facilitates the hive connection from our Gateway.

#### nube-server-ditto-driver

- Connects to the ditto-server and listens its web-socket and published that into our EventBus.

#### nube-edge-ditto-driver (Demo)

- Just a demo for edge devices

## HOW TO

- [Running Local](./HOWTO.md#Run-Local)
- [Development](./HOWTO.md#Development)
- [Deployment](./HOWTO.md#Deployment)