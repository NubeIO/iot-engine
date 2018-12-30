package com.nubeiot.core.kafka;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class KafkaComponent implements IComponent {

    private static final Logger logger = LoggerFactory.getLogger(KafkaComponent.class);

    private final Vertx vertx;
    private final KafkaConfig kafkaConfig;

    @Override
    public void start() throws NubeException {
    }

    @Override
    public void stop() throws NubeException {

    }

}
