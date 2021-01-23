# Dashboard Kafka Sample

This module is mock `dashboard` server:

- Provide simple `index.html` static page
- Stream data from `Kafka cluster` then publish it to `websocket`.

## How To

- Follow setting up Docker environment [docker section](https://github.com/NubeIO/iot-engine/wiki/Dev-%7C-Docker)

- Start `kafka`, `edge` in [`sandbox`](../../../../sandbox)

- After starting, webapp should be available at: [http://localhost:8280/web/index.html](http://localhost:8280/web/index.html)
