package com.nubeiot.core.workflow;

import io.reactivex.annotations.Experimental;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;

@Experimental
public interface ServiceRecord extends JsonData {

    String type();

    JsonObject location();

    JsonObject metadata();

    interface HttpClientRecord extends ServiceRecord {

        @Override
        default String type() {
            return "http-endpoint";
        }

    }

}
