package com.nubeiot.iotdata.unit;

public class HPADataType extends NumberDataType implements Pressure {

    HPADataType() {
        super("hPa", "hPa");
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
