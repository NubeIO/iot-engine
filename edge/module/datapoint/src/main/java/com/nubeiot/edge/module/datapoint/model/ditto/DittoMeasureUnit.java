package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IMeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;

import lombok.NonNull;

public final class DittoMeasureUnit extends AbstractDittoModel<IMeasureUnit> {

    public DittoMeasureUnit(@NonNull MeasureUnit data) {
        super(data);
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/units/properties";
    }

}
