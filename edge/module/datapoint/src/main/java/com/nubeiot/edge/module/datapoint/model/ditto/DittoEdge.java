package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
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
        metadata.remove(DataSyncConfig.NAME);
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
