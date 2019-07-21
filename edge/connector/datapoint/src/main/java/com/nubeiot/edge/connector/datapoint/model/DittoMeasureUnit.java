package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.edge.connector.datapoint.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IMeasureUnit;

import lombok.NonNull;

public final class DittoMeasureUnit extends AbstractDittoModel<IMeasureUnit> {

    public DittoMeasureUnit(@NonNull IMeasureUnit data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        return null;
    }

}
