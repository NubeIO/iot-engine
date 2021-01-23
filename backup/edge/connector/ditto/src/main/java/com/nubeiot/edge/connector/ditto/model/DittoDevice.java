package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IDevice;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DittoDevice extends AbstractDittoModel<IDevice> {

    public DittoDevice(@NonNull Device data) {
        super(data);
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/equipments/properties" + get().getId();
    }

}
