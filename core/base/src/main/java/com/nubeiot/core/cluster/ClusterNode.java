package com.nubeiot.core.cluster;

import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClusterNode {

    private final String id;
    private final String name;
    private final String address;
    private final String localAddress;

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
