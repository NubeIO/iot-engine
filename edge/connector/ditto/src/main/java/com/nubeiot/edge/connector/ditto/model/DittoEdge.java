package com.nubeiot.edge.connector.ditto.model;

import java.util.Map;

import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IEdge;
import com.nubeiot.iotdata.edge.model.tables.pojos.Edge;

import lombok.NonNull;

public final class DittoEdge extends AbstractDittoModel<IEdge> {

    public DittoEdge(@NonNull Edge data) {
        super(data);
    }

    @JsonCreator
    public static DittoEdge create(Map<String, Object> settings) {
        return new DittoEdge(new Edge(JsonData.tryParse(settings).toJson()));
    }

    @Override
    public JsonObject toJson() {
        JsonObject metadata = get().getMetadata();
        //TODO https://github.com/NubeIO/iot-engine/issues/140
        metadata.remove("__data_sync__");
        return JsonPojo.from(get().setMetadata(metadata).setSyncAudit(null)).toJson();
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/attributes/extra";
    }

    public String creationEndpoint(String thingId) {
        return Strings.format("/api/2/things/{0}", Strings.requireNotBlank(thingId).toLowerCase());
    }

}
