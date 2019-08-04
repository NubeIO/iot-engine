package com.nubeiot.iotdata.unit;

import java.util.List;
import java.util.Map;

interface InternalDataType extends DataType {

    InternalDataType setPossibleValues(Map<Double, List<String>> possibleValues);

}
