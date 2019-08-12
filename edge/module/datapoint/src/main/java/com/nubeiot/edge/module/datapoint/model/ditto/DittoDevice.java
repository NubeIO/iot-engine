package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IDevice;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DittoDevice extends AbstractDittoModel<IDevice> {

    public DittoDevice(@NonNull IDevice data) {
        super(data);
    }

    @JsonCreator
    public static DittoDevice create(Map<String, Object> settings) {
        return new DittoDevice(new Device(JsonData.tryParse(settings).toJson()));
    }

    @Override
    public String endpoint(String thingId) {
        return "/things/" + thingId + "/attributes";
    }

}
