package com.nubeiot.edge.connector.bonescript.constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BBPinMapping {

    public abstract List<String> getAnalogInPins();

    public abstract List<String> getAnalogOutPins();

    public abstract List<String> getDigitalInPins();

    public abstract List<String> getDigitalOutPins();

    public List<String> getFieldsKey(Field[] fields) {
        List<String> values = new ArrayList<>();
        for (Field field : fields) {
            if (!field.isSynthetic()) {
                values.add(field.getName());
            }
        }
        return values;
    }

    public List<String> getAllPins() {
        List<String> values = getAnalogInPins();
        values.addAll(getAnalogOutPins());
        values.addAll(getDigitalInPins());
        values.addAll(getDigitalOutPins());
        return values;
    }

    public List<String> getInputPins() {
        List<String> values = getAnalogInPins();
        values.addAll(getDigitalInPins());
        return values;
    }

    public List<String> getOutputPins() {
        List<String> values = getAnalogOutPins();
        values.addAll(getDigitalOutPins());
        return values;
    }

    public List<String> getAnalogPins() {
        List<String> values = getAnalogInPins();
        values.addAll(getAnalogOutPins());
        return values;
    }

    public List<String> getDigitalPins() {
        List<String> values = getDigitalInPins();
        values.addAll(getDigitalOutPins());
        return values;
    }

    public List<String> getInputTypes() {
        String[] values = {"0-10dc", "4-20ma", "10k thermistor", "digital"};
        return Arrays.asList(values);
    }

    public List<String> getOutputTypes() {
        String[] values = {"0-12dc", "0-10dc", "digital"};
        return Arrays.asList(values);
    }

    public int getOutgoingPort() {
        return 18000;
    }

    // todo: test
    public List<String> getValidTrueValues() {
        String[] values = {"1", "true", "on", "start"};
        return Arrays.asList(values);
    }

    // todo: test
    public List<String> getValidFalseValues() {
        String[] values = {"0", "false", "off", "stop"};
        return Arrays.asList(values);
    }

    // todo: test
    public List<String> getValidPinOutputs() {
        List<String> values = getValidTrueValues();
        values.addAll(getValidFalseValues());
        values.add("null");
        return values;
    }

}
