package com.nubeiot.edge.connector.bonescript.utils;

import static com.nubeiot.edge.connector.bonescript.BoneScriptVerticle.BB_DEFAULT_VERSION;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DEFAULT_VALUE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY_ARRAY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;
import com.nubeiot.edge.connector.bonescript.enums.BBVersion;

import io.vertx.core.json.JsonObject;

public class PointUtils {

    private static final String PRIORITY_ARRAY_TEMPLATE = "ditto/points/properties/priority_array_template.json";

    public static void setValueAndPriority(String id, JsonObject point) {
        JsonObject priorityArrayTemplate = new JsonObject(
            Strings.convertToString(Reflections.staticClassLoader().getResourceAsStream(PRIORITY_ARRAY_TEMPLATE)));

        if (!point.containsKey(PRIORITY_ARRAY)) {
            point.put(PRIORITY_ARRAY, priorityArrayTemplate);
        }

        String value = point.getString(DEFAULT_VALUE, "0");

        for (int i = 0; i <= 16; i++) {
            String val = point.getJsonObject(PRIORITY_ARRAY).getString(Integer.toString(i));
            if (!val.equals("null")) {
                value = val;
                point.put(PRIORITY, i);
                break;
            }
        }

        BBPinMapping bbPinMapping = BBVersion.getBbPinMapping(BB_DEFAULT_VERSION);

        if (bbPinMapping.getAnalogOutPins().contains(id)) {
            ScaleUtils.analogOutput(point, value);
        } else if (bbPinMapping.getDigitalOutPins().contains(id)) {
            ScaleUtils.digitalOutput(point, value);
        } else {
            if (Strings.isNumeric(value)) {
                point.put(VALUE, Double.parseDouble(value));
            } else {
                point.put(VALUE, value);
            }
        }
    }

}
