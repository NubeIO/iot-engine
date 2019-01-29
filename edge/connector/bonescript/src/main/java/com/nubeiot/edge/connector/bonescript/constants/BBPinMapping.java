package com.nubeiot.edge.connector.bonescript.constants;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;


public abstract class BBPinMapping {

    public abstract Set<String> getAnalogInPins();

    public abstract Set<String> getAnalogOutPins();

    public abstract Set<String> getDigitalInPins();

    public abstract Set<String> getDigitalOutPins();

    Set<String> getFieldsKey(Field[] fields) {
        Set<String> values = new LinkedHashSet<>();
        for (Field field : fields) {
            if (!field.isSynthetic()) {
                values.add(field.getName());
            }
        }
        return values;
    }

    public final Set<String> getAllPins() {
        Set<String> values = getAnalogInPins();
        values.addAll(getAnalogOutPins());
        values.addAll(getDigitalInPins());
        values.addAll(getDigitalOutPins());
        return values;
    }

    public final Set<String> getInputPins() {
        Set<String> values = getAnalogInPins();
        values.addAll(getDigitalInPins());
        return values;
    }

    public final Set<String> getOutputPins() {
        Set<String> values = getAnalogOutPins();
        values.addAll(getDigitalOutPins());
        return values;
    }

    public final Set<String> getAnalogPins() {
        Set<String> values = getAnalogInPins();
        values.addAll(getAnalogOutPins());
        return values;
    }

    public final Set<String> getDigitalPins() {
        Set<String> values = getDigitalInPins();
        values.addAll(getDigitalOutPins());
        return values;
    }

    public final Set<String> getInputTypes() {
        String[] values = {"0-10dc", "4-20ma", "10k thermistor", "digital"};
        return new HashSet<>(Arrays.asList(values));
    }

    public final Set<String> getOutputTypes() {
        String[] values = {"0-12dc", "0-10dc", "digital"};
        return new HashSet<>(Arrays.asList(values));
    }

    public final int getOutgoingPort() {
        return 18000;
    }

    public final Set<Object> getValidTrueValues() {
        Object[] values = {1, true, "1", "true", "on", "start"};
        return new HashSet<>(Arrays.asList(values));
    }

    public final Set<Object> getValidFalseValues() {
        Object[] values = {0, false, "0", "false", "off", "stop"};
        return new HashSet<>(Arrays.asList(values));
    }

    public final Set<Object> getValidPinOutputs() {
        Set<Object> values = new HashSet<>();
        Stream.of(getValidTrueValues(), getValidFalseValues()).forEach(values::addAll);
        values.add("null");
        return values;
    }

}
