package com.nubeiot.edge.connector.bonescript.utils;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.OFFSET;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PIN_VALUE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TYPE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import com.nubeiot.core.utils.SQLUtils;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.json.JsonObject;

public class ScaleUtils {

    public static void analogOutput(JsonObject point, String value) {
        if (Strings.isNumeric(value)) {
            double numericValue = Double.parseDouble(value);

            if (point.containsKey(TYPE)) {
                switch (point.getString(TYPE).toLowerCase()) {
                    case "0-12dc":
                        JsonObject scaleResult = scaleToPinValue(numericValue, 0d, 1d, 0d, 12d);
                        setValueAndPinValue(point, scaleResult.getInteger(VALUE), scaleResult.getInteger(PIN_VALUE));
                        break;
                    case "0-10dc":
                        scaleResult = scaleToPinValue(numericValue, 0d, 0.83333333333333333333331d, 0d, 10d);
                        setValueAndPinValue(point, scaleResult.getDouble(VALUE), scaleResult.getInteger(PIN_VALUE));
                        break;
                    case "digital":
                        if (numericValue < 0.5) {
                            setValueAndPinValue(point, 0d, 0d);
                        } else {
                            setValueAndPinValue(point, 1d, 1d);
                        }
                        break;
                    default:
                        defaultNumericScaling(point, numericValue);
                }
            } else {
                defaultNumericScaling(point, numericValue);
            }
        } else {
            value = value.toLowerCase();

            if (value.equals("null")) {
                point.put(VALUE, value);
                point.put(PIN_VALUE, 0);
            } else if (SQLUtils.in(value, "true", "on", "start")) {
                if (point.containsKey(TYPE)) {
                    switch (point.getString(TYPE).toLowerCase()) {
                        case "0-12dc":
                            setValueAndPinValue(point, 12d, 1d);
                            break;
                        case "0-10dc":
                            setValueAndPinValue(point, 10d, 0.8333333333333333333333d);
                            break;
                        case "digital":
                            setValueAndPinValue(point, 1d, 1d);
                            break;
                        default:
                            setValueAndPinValue(point, 12d, 1d);
                    }
                } else {
                    setValueAndPinValue(point, 12d, 1d);
                }
            } else if (SQLUtils.in(value, "false", "off", "stop")) {
                setValueAndPinValue(point, 0d, 0d);
            }
        }

        if (point.containsKey(OFFSET)) {
            point.put(VALUE, point.getDouble(VALUE) + point.getDouble(OFFSET));
        }
    }

    public static void digitalOutput(JsonObject point, String value) {
        if (Strings.isNumeric(value)) {
            double numericValue = Double.parseDouble(value);
            if (numericValue == 1d || numericValue == 0d) {
                setValueAndPinValue(point, numericValue, numericValue);
            } else {
                if (value.equalsIgnoreCase("null")) {
                    setValueAndPinValue(point, numericValue, 0d);
                } else if (SQLUtils.in(value, true, "true", "on", "start")) {
                    setValueAndPinValue(point, 1, 1);
                } else if (SQLUtils.in(value, true, "false", "off", "stop")) {
                    setValueAndPinValue(point, 0, 0);
                }
            }
        }
    }

    private static void setValueAndPinValue(JsonObject point, double value, double pinValue) {
        point.put(VALUE, value);
        point.put(PIN_VALUE, pinValue);
    }

    private static void defaultNumericScaling(JsonObject point, double numericValue) {
        if (numericValue >= 0.0 && numericValue <= 12.0) {
            setValueAndPinValue(point, numericValue, numericValue);
        } else if (numericValue < 0.0) {
            setValueAndPinValue(point, 0, 0);
        } else if (numericValue > 12.0) {
            setValueAndPinValue(point, 12, 1);
        }
    }

    private static JsonObject scaleToPinValue(double value, double minOutput, double maxOutput) {
        return scaleToPinValue(value, minOutput, maxOutput, 0, 1);
    }

    private static JsonObject scaleToPinValue(double value, double minOutput, double maxOutput, double minInput,
                                              double maxInput) {
        double pinValue = (maxOutput - minInput) * (value - minInput) / (maxInput - minInput) + minOutput;
        if (pinValue > maxOutput) {
            pinValue = maxOutput;
            value = maxInput;
        } else if (pinValue < minOutput) {
            pinValue = minOutput;
            value = minInput;
        }
        return new JsonObject().put(PIN_VALUE, pinValue).put(VALUE, value);
    }

}
