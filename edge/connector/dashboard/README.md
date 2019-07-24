# Edge Dashboard App

This is an `App` for installing `Edge Dashboard`.

## Basic Concept 
A Java server which wraps the `Edge Dashboard`'s built static files inside a folder for serving it as frontend resource.

## Installation example
`POST`: `http://{{edge_gateway_api}}/api/services`
```
{
  "metadata": {
    "group_id": "com.nubeiot.edge.connector",
    "artifact_id": "dashboard",
    "service_name": "edge-connector-dashboard",
    "version": "1.0.0-SNAPSHOT"
  }
}
```

## Endpoints

Have some endpoint:

- `GET (http://{{edge_dashboard_api}}/api/edge-dashboard/connection)`: For getting dynamic connection details

`Output:`
```
{
    "records": [
        {
            "id": 1,
            "gateway_schema": "http",
            "gateway_host": "localhost",
            "gateway_port": 8380,
            "gateway_api": "/gw2",
            "gateway_root_api": "/api",
            "nodered_schema": "http",
            "nodered_host": "localhost",
            "nodered_port": 1880,
            "created_at": "2019-11-27T17:58:08.042Z",
            "modified_at": "2019-11-27T17:58:33.152Z"
        }
    ]
}
```

`------------------------------------------------------------------------------`
- `PATCH (http://{{edge_dashboard_api}}/api/edge-dashboard/connection/1)`: For patch connection detail:

`Body:`
```
{
    "gateway_schema": "http",
    "gateway_host": "localhost",
    "gateway_api": "/gw",
    "gateway_port": 8380,
    "gateway_root_api": "/api",
    "nodered_schema": "http",
    "nodered_host": "localhost",
    "nodered_port": 1880
}
```

