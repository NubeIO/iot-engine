package com.nubeiot.core.event;

import java.io.Serializable;

/**
 * {@code Eventbus} pattern mode.
 */
public enum EventPattern implements Serializable {

    PUBLISH_SUBSCRIBE, POINT_2_POINT, REQUEST_RESPONSE

}
