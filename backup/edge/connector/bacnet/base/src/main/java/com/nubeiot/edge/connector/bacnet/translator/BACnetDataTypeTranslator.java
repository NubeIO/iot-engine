package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.iotdata.translator.DataTypeTranslator;
import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.iotdata.unit.DataTypeCategory.Base;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;

//TODO implement it
public final class BACnetDataTypeTranslator
    implements BACnetTranslator<DataType, EngineeringUnits>, DataTypeTranslator<DataType, EngineeringUnits> {

    @Override
    public DataType serialize(EngineeringUnits unit) {
        Objects.requireNonNull(unit, "Invalid BACnet engineering unit type");
        return Base.NUMBER;
    }

    @Override
    public EngineeringUnits deserialize(DataType dataType) {
        return null;
    }

    @Override
    public Class<DataType> fromType() {
        return DataType.class;
    }

    @Override
    public Class<EngineeringUnits> toType() {
        return EngineeringUnits.class;
    }

}
