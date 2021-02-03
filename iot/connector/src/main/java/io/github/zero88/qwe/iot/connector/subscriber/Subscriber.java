package io.github.zero88.qwe.iot.connector.subscriber;

import io.github.zero88.qwe.dto.JsonData;

public interface Subscriber extends JsonData {

    SubscriberType getType();

    String getCode();

    default String key() {
        return getType().type() + "::" + getCode();
    }

}
