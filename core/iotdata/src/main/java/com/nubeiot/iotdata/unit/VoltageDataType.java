package com.nubeiot.iotdata.unit;

public final class VoltageDataType extends NumberDataType implements Power {

    VoltageDataType() { super("voltage", "V"); }

    @Override
    public Double parse(Object data) {
        return super.parse(data);
    }

    @Override
    public String display(Double value) {
        return super.display(value);
    }

}
