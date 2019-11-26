package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.iotdata.translator.DataTypeTranslator;
import com.nubeiot.iotdata.unit.DataType;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;

public final class BACnetDataTypeTranslator
    implements BACnetTranslator<DataType, EngineeringUnits>, DataTypeTranslator<DataType, EngineeringUnits> {

    @Override
    public DataType serialize(EngineeringUnits unit) {
        return null;
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
