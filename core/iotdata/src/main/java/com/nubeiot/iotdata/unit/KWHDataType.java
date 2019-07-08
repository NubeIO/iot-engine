package com.nubeiot.iotdata.unit;

public final class KWHDataType extends NumberDataType implements Power {

    public KWHDataType() {
        super("KWH", "kWh");
    }

    @Override
    public Double parse(Object data) {
        return super.parse(data);
    }

    @Override
    public String display(Double value) {
        return super.display(value);
    }

}
