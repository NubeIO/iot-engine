# NubeIO IoT Platform

[![Build Status](https://jenkins.nube-io.com/buildStatus/icon?job=NubeIO/iot-engine/master)](https://jenkins.nube-io.com/job/NubeIO/iot-engine/master)

IoT Platform provides a distributed system based on micro service that helps to:

- Lightweight and reliable deployments on hundred `edge device` remotely, while your devices remain online.
- Collect and store IoT data sensors by realtime in time-series database.
- Data Analytics and Data Visualization.

## Architecture

Use `vert.x` framework for building reactive applications with modularization, non-blocking, small footprint.

IoT platform is divided as structure: `core`, `edge`, `dashboard`

### IoT Core

Contains `core` components with utilities to reuse everywhere. Each `core` components is one project, that is responsible for set of tasks that same functionality and able to include as dependency in another components.

### IoT Edge

There are some projects live in `edge` device:

- `core`: core projects for sharing code between `bios` and `installer`
- `bios`: is executable application, that is most importance. It is main process in `edge` for manage other resources.
- `module > installer`: manage `services` on edge devices, it is installed on `bios` in default
- `Edge services` is libraries that provides sensor data, statistics ana analytics data, It contains:
  - `connector`: parent projects contains `connector` project for third-party services
  - `rule`: parent projects contains `rule` project for define and execute computation process.

App developers use Maven to write apps and install apps (jars) to `maven` repository. Based on user's request, it is able to install one or more `services` on `edge`.

Following is the rough architecture.

![nube-vertx-engine architecture](docs/nube-app-store.png?raw=true "Architecture of Nube vert.x Engine")

### IoT Dashboard

Dashboard for manage customer metadata and provide a set of toolkits for statistic, analytics, visualize IoT data.

It includes:

- `server`: Dashboard REST API server
- `connector`: parent projects contains `connector` project for third-party services

## HOW TO

- [Running Local](./HOWTO.md#Run-Local)
- [Development](./HOWTO.md#Development)
- [Deployment](./HOWTO.md#Deployment)
