package com.nubeiot.iotdata.unit;

public final class DBMDataType extends NumberDataType implements Power {

    DBMDataType() { super("DBM", "dBm"); }

    @Override
    public Double parse(Object data) {
        return super.parse(data);
    }

    @Override
    public String display(Double value) {
        return super.display(value);
    }

}
