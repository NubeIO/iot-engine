package com.nubeiot.edge.connector.datapoint.model.ditto;

import com.nubeiot.edge.connector.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IEquipment;

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
