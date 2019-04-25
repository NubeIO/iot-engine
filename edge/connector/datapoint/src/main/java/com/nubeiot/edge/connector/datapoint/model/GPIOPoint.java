package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.edge.connector.datapoint.model.DataPoint.DefaultDataPoint;
import com.nubeiot.edge.connector.datapoint.model.tables.interfaces.IPoint;

public class GPIOPoint extends DefaultDataPoint {

    public GPIOPoint(IPoint point) {
        super(point);
    }

}
