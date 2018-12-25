package com.nubeiot.core.kafka;

import com.nubeiot.core.component.IComponentProvider;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface KafkaProvider extends IComponentProvider {

    String KAFKA_CFG_NAME = "__kafka__";

    static KafkaComponent create(Vertx vertx, JsonObject rootCfg) {
        JsonObject kafkaCfg = IComponentProvider.computeConfig("kafka.json", KAFKA_CFG_NAME, rootCfg);
        return new KafkaComponent(vertx.getDelegate(), kafkaCfg);
    }

}
