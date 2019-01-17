package com.nubeiot.core.kafka.handler.producer;

import io.vertx.core.AsyncResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.RecordMetadata;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.exceptions.ErrorMessage;

/**
 * @see KafkaProducerHandler
 */
public abstract class AbstractKafkaProducerHandler extends AbstractSharedDataDelegate implements KafkaProducerHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public final void handle(AsyncResult<RecordMetadata> result) {
        if (result.succeeded()) {
            handleSuccess(result.result());
        } else {
            handleFailed(ErrorMessage.parse(result.cause()));
        }
    }

}
