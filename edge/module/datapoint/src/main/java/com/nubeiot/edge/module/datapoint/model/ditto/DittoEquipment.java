package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IEquipment;
import com.nubeiot.iotdata.edge.model.tables.pojos.Equipment;

import lombok.NonNull;

public final class DittoEquipment extends AbstractDittoModel<IEquipment> {

    public DittoEquipment(@NonNull Equipment data) {
        super(data);
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/equipments/properties" + get().getId();
    }

}
