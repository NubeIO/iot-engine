# Device App

This is an `App` for setting static/dynamic `IP` address.

## Installation example
`POST`: `http://{{edge_gateway_api}}/api/services`
```
{
  "metadata": {
    "group_id": "com.nubeiot.edge.connector",
    "artifact_id": "device",
    "service_name": "edge-connector-network",
    "version": "1.0.0-SNAPSHOT"
  }
}
```

## Endpoints

Have some endpoint:

1. `POST`: http://localhost:8080/api/network/ip: To post new IP, body example
```
{
  "ip-address": "192.168.1.100",
  "subnet-mask": "255.255.0.0",
  "gateway": "8.8.8.8"
}
```
2. `POST`: http://localhost:8080/api/network/ip: To reset IP with `DHCP` config
