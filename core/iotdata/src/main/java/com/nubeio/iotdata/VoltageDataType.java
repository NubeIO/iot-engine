package com.nubeio.iotdata;

public final class VoltageDataType extends NumberDataType implements Power {

    VoltageDataType() { super("VOLTAGE", "V"); }

    @Override
    public Double parse(Object data) {
        return super.parse(data);
    }

    @Override
    public String display(Double value) {
        return super.display(value);
    }

}
