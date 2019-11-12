package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IDevice;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DittoDevice extends AbstractDittoModel<IDevice> {

    public DittoDevice(@NonNull Device data) {
        super(data);
    }

    @JsonCreator
    public static DittoDevice create(Map<String, Object> settings) {
        return new DittoDevice(new Device(JsonData.tryParse(settings).toJson()));
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/attributes/extra";
    }

    @Override
    public JsonObject toJson() {
        JsonObject metadata = get().getMetadata();
        metadata.remove(DataSyncConfig.NAME);
        return JsonPojo.from(get().setMetadata(metadata).setSyncAudit(null)).toJson();
    }

    public String creationEndpoint(String thingId) {
        return Strings.format("/api/2/things/{0}", Strings.requireNotBlank(thingId).toLowerCase());
    }

}
