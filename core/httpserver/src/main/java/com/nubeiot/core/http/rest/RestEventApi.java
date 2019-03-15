package com.nubeiot.core.http.rest;

import java.util.List;

/**
 * Make a mapping dynamically between {@code HTTP endpoint} and {@code EventBus}
 */
public interface RestEventApi extends ActionMethodMapping {

    List<RestEventMetadata> getRestMetadata();

}
