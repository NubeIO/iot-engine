# NubeIO IoT Platform

[![Build Status](https://jenkins.nube-io.com/buildStatus/icon?job=NubeIO/iot-engine/master)](https://jenkins.nube-io.com/job/NubeIO/iot-engine/master)

IoT Platform provides a distributed system based on micro service that helps to:

- Lightweight and reliable deployments on hundred `edge device` remotely, while your devices remain online.
- Collect and store IoT data sensors by realtime in time-series database.
- Data Analytics and Data Visualization.

## Architecture

Go through [Architecture-Overview](https://github.com/NubeIO/iot-engine/wiki/Architecture-Overview)

## How to contribute

Must read:
- [Working Process](https://github.com/NubeIO/iot-engine/wiki/Working-Process)
- [Development Note](https://github.com/NubeIO/iot-engine/wiki/Development-Note)

## Instruction 

### Install BACnet on PI

- Install `java`
  ```bash
  sudo apt update -y \
    && sudo apt install openjdk-8-jre -y \
    && sudo apt autoclean -y \
    && sudo apt autoremove -y
  ```
- Download binary into `/tmp`

  ```bash
  sudo curl -L https://github.com/zero88/gh-release-downloader/releases/download/v1.1.1/ghrd -o /usr/local/bin/ghrd \
    && sudo chmod +x /usr/local/bin/ghrd \
    && sudo ln -s /usr/local/bin/ghrd /usr/bin/ghrd \
    && sudo apt install jq -y
  
  ghrd -a .*bacnet.* -x -t <token_if_repo_is_private> NubeIO/iot-engine -o /tmp
  ```

- Register it as service by one liner:

  ```bash
  sudo mkdir -p /app/ \
    && u=$(whoami) \
    && sudo chown -R $u:$u /app \
    && unzip -d /app /tmp/nubeio-edge-connector-bacnet-0.2.1.zip \
    && mv /app/nubeio-edge-connector-bacnet-0.2.1 /app/bacnet \
    && sudo cp -rf /app/bacnet/conf/nubeio-bacnet.service /etc/systemd/system \
    && sudo systemctl daemon-reload \
    && sudo systemctl enable nubeio-bacnet.service \
    && sudo systemctl restart nubeio-bacnet.service
  ```

- Application will start at port: `8888`
- Verify service: `systemctl status nubeio-bacnet.service`
- Verify port: `netstat -tupln | grep 8888`
- Verify service: `curl -i localhost:8888/gw/index?_pretty=true`
- Any configuration can be modified at `/app/conf/bacnet.json`.
  Example [config](https://github.com/NubeIO/iot-engine/blob/f946182ade3d968eff929b6189ceb97360656ace/edge/connector/bacnet/src/main/resources/bacnet.json)
- Example [API](https://documenter.getpostman.com/view/670606/RWguwGk8#6a83deec-896d-4052-abf3-4e8e26753d66). Remember
  change it to `localhost:8888`

### Tweak config

Assume you install `jar` file in `/app/bacnet`.

- **Logging**: `/app/bacnet/conf/logback.xml`
    - Change log level to `info` => `error`:

      ```bash
      sed -i 's/info/error/g' /app/bacnet/conf/logback.xml
      ```

- **App config**: `/app/bacnet/conf/bacnet.json`
    - Change log level to `8888` => `9999`:

      ```bash
      sed -i 's/"port": 8888/"port": 9999/g' /app/bacnet/conf/bacnet.json
      ```

    - Fixed Bacnet Device Id to `81234` (normally it is random number from `80000` to `90000`)

      ```bash
      jq --arg deviceId 81234 '.__app__.__bacnet__ += {deviceId: $deviceId}' < conf/bacnet.json > /tmp/bacnet.json \
        && cp -rf /tmp/bacnet.json /app/bacnet/conf/bacnet.json
      ```

- **Service**: `/app/bacnet/conf/nubeio-bacnet.service`
    - Change `memory usage` from `150M` to `200Mb`

      ```bash
      sed -i 's/-XX:MaxRAM=150m/-XX:MaxRAM=200m/g' /app/bacnet/conf/nubeio-bacnet.service
      ```

All changes then require restart service.

```bash
sudo systemctl restart nubeio-bacnet.service
```

In case of update `memory usage` in `conf`, need to re-update service by

```bash
sudo cp -rf /app/bacnet/conf/nubeio-bacnet.service /etc/systemd/system \
    && sudo systemctl daemon-reload \
    && sudo systemctl enable nubeio-bacnet.service \
    && sudo systemctl restart nubeio-bacnet.service
```
