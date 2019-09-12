package com.nubeiot.iotdata.unit;

import java.util.Objects;

public final class BooleanDataType extends NumberDataType {

    BooleanDataType() {
        super("bool", null);
    }

    @Override
    public Double parse(Object data) {
        if (Objects.isNull(data)) {
            return 0d;
        }
        if (data instanceof Number) {
            return ((Number) data).doubleValue() > 0 ? 1d : 0d;
        }
        if (data instanceof Boolean) {
            return Boolean.TRUE == data ? 1d : 0d;
        }
        if (data instanceof String) {
            return Boolean.TRUE.equals(Boolean.valueOf((String) data)) ? 1d : 0d;
        }
        return 0d;
    }

}
