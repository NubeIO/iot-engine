# Dashboard Kafka Sample

This module is mock `dashboard` server:

- Provide simple `index.html` static page
- Stream data from `Kafka cluster` then publish it to `websocket`.

## How To

- Start `kafka`, `nexus` Docker follow by [docker section](https://github.com/NubeIO/iot-engine/wiki/Dev-%7C-Docker)
- Build project `gradle clean jooq build uberJar publish`
- Copy artifact `cp -rf build/libs/nube-dashboard-connector-sample-kafka-1.0.0-SNAPSHOT-fat.jar demo/dashboard-kafka.jar`
- Start `java -Dlogback.configurationFile=logback-dashboard-kafka.xml -jar dashboard-kafka.jar -conf dashboard-kafka.json`

After starting, webserver should be available at: [http://localhost:8100/sample/index.html](http://localhost:8100/sample/index.html)
