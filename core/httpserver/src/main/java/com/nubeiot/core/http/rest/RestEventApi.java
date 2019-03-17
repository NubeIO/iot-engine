package com.nubeiot.core.http.rest;

import java.util.List;

import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.RestEventMetadata;

/**
 * Make a mapping dynamically between {@code HTTP endpoint} and {@code EventBus}
 */
public interface RestEventApi extends ActionMethodMapping {

    List<RestEventMetadata> getRestMetadata();

}
