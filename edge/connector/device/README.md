# Device App

This is an `App` for getting device, network status of `BBB` and setting static/dynamic `IP` address.

## Installation example
`POST`: `http://{{edge_gateway_api}}/api/services`
```
{
  "metadata": {
    "group_id": "com.nubeiot.edge.connector",
    "artifact_id": "device",
    "service_name": "edge-connector-device",
    "version": "1.0.0-SNAPSHOT"
  }
}
```

## Endpoints

Have some endpoint:

1. `GET`: http://localhost:8080/api/device/status: To get device status. Device memory, IP info like
 arp and
 ip addr, Top running process
2. `GET`: http://localhost:8080/api/device/network: To get network info
3. `POST`: http://localhost:8080/api/device/ip: To post new IP, body example
```
{
  "ipAddress":"",
  "subnetMask":"",
  "gateway":""
}
```
4. `POST`: http://localhost:8080/api/device/dhcp: To reset IP with `DHCP` config, send body as `null`
