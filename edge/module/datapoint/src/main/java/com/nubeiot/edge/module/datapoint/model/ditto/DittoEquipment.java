package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IEquipment;

import lombok.NonNull;

public final class DittoEquipment extends AbstractDittoModel<IEquipment> {

    public DittoEquipment(@NonNull IEquipment data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        return "/things/" + thingId + "/features/sensorList/properties";
    }

}
