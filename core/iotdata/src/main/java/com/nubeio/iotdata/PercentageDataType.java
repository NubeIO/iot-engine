package com.nubeio.iotdata;

public final class PercentageDataType extends NumberDataType {

    PercentageDataType() { super("PERCENTAGE", "%"); }

    @Override
    public Double parse(Object data) {
        return super.parse(data);
    }

    @Override
    public String display(Double value) {
        return super.display(value);
    }

}
