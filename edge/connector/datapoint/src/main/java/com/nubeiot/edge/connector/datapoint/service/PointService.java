package com.nubeiot.edge.connector.datapoint.service;

import com.nubeiot.iotdata.model.tables.interfaces.IPoint;

public class PointService extends AbstractModelService<IPoint> {

    @Override
    public String endpoint() {
        return "/point";
    }

}
