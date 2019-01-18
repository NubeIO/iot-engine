package com.nubeiot.core.kafka.handler;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.kafka.ClientTechId;
import com.nubeiot.core.kafka.KafkaClientType;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class LoggingErrorHandler<K, V> implements KafkaErrorHandler<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingErrorHandler.class);

    private final KafkaClientType type;

    @Override
    public void accept(ClientTechId<K, V> techId, String clientId, Throwable throwable) {
        logger.error("Error in Kafka {} :: Client ID {} :: Technical ID {}", throwable, type, clientId, techId);
    }

}
