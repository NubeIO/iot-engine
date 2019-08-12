package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IMeasureUnit;

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
