package com.nubeiot.edge.connector.bonescript.utils;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DEFAULT_VALUE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY_ARRAY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bonescript.SingletonBBPinMapping;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;

import io.vertx.core.json.JsonObject;

public class PointUtils {

    private static final String PRIORITY_ARRAY_TEMPLATE = "ditto/points/properties/priority_array_template.json";

    public static void setValueAndPriority(String id, JsonObject point) {
        JsonObject priorityArrayTemplate = new JsonObject(
            Strings.convertToString(Reflections.staticClassLoader().getResourceAsStream(PRIORITY_ARRAY_TEMPLATE)));

        if (!point.containsKey(PRIORITY_ARRAY)) {
            point.put(PRIORITY_ARRAY, priorityArrayTemplate);
        }

        Object value = point.getValue(DEFAULT_VALUE, 0);

        for (int i = 1; i <= 16; i++) {
            Object val = point.getJsonObject(PRIORITY_ARRAY).getValue(Integer.toString(i));
            if (!val.equals("null")) {
                value = val;
                point.put(PRIORITY, i);
                break;
            }
        }

        BBPinMapping bbPinMapping = SingletonBBPinMapping.getInstance();

        if (bbPinMapping.getAnalogOutPins().contains(id)) {
            ScaleUtils.analogOutput(point, value);
        } else if (bbPinMapping.getDigitalOutPins().contains(id)) {
            ScaleUtils.digitalOutput(point, value);
        } else {
            if (Strings.isNumeric(value.toString())) {
                point.put(VALUE, Double.parseDouble(value.toString()));
            } else {
                point.put(VALUE, value);
            }
        }
    }

}
