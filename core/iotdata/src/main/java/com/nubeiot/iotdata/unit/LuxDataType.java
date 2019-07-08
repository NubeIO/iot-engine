package com.nubeiot.iotdata.unit;

public final class LuxDataType extends NumberDataType implements Illuminance {

    LuxDataType() {
        super("LUX", "lx");
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
