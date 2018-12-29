package com.nubeiot.core.kafka;

import com.nubeiot.core.component.IComponentProvider;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public interface KafkaProvider extends IComponentProvider {

    static KafkaComponent create(Vertx vertx, JsonObject rootCfg) {
        KafkaConfig kafkaCfg = IComponentProvider.computeConfig("kafka.json", KafkaConfig.class, rootCfg);
        return new KafkaComponent(vertx.getDelegate(), kafkaCfg);
    }

}
