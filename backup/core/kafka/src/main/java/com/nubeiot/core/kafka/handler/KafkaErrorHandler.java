package com.nubeiot.core.kafka.handler;

import com.nubeiot.core.kafka.ClientTechId;
import com.nubeiot.core.kafka.KafkaClientType;

public interface KafkaErrorHandler<K, V> {

    KafkaErrorHandler CONSUMER_ERROR_HANDLER = new LoggingErrorHandler(KafkaClientType.CONSUMER);
    KafkaErrorHandler PRODUCER_ERROR_HANDLER = new LoggingErrorHandler(KafkaClientType.PRODUCER);

    void accept(ClientTechId<K, V> techId, String clientId, Throwable throwable);

}
