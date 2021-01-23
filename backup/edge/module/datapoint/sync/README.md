# Edge Datapoint Sync

Java API library defines `edge data-point-sync`. 

It supports bi-directional synchronization data by corresponding 2 `interfaces`:

 - Listen an `event` from `cloud service` then dispatch to `data-point` service by `DITTO`, `KAFKA`, `MQTT`, etc
 - Listen an `event` in `edge data-point` per each service then dispatch to the registered `cloud service`
