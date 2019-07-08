package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.iotdata.dto.PointCategory;
import com.nubeiot.iotdata.model.tables.interfaces.IPoint;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface DataPoint extends JsonData {

    static DataPoint factory(@NonNull IPoint point) {
        if (PointCategory.GPIO.equals(point.getPointCategory())) {
            return new GPIOPoint(point);
        }
        if (PointCategory.BACNET.equals(point.getPointCategory())) {
            return new GPIOPoint(point);
        }
        if (PointCategory.MODBUS.equals(point.getPointCategory())) {
            return new GPIOPoint(point);
        }
        if (PointCategory.HAYSTACK.equals(point.getPointCategory())) {
            return new GPIOPoint(point);
        }
        return new DefaultDataPoint(point);
    }

    IPoint unwrap();

    @RequiredArgsConstructor
    class DefaultDataPoint implements DataPoint {

        private final IPoint point;

        @Override
        public final IPoint unwrap() {
            return point;
        }

    }

}
