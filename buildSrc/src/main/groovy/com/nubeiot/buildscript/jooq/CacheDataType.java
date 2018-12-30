package com.nubeiot.buildscript.jooq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import lombok.Getter;

@Getter
public class CacheDataType {

    private static CacheDataType instance;

    private CacheDataType() {
        this.addDataType(java.util.Date.class.getName(), "%s.getTime()",
                         "Date.from(java.time.Instant.ofEpochMilli(json.getLong(\"%s\")))");
        this.addDataType(java.time.Instant.class.getName(), "%s.toEpochMilli()",
                         "java.time.Instant.ofEpochMilli(json.getLong(\"%s\"))");
    }

    public static synchronized CacheDataType instance() {
        return instance == null ? instance = new CacheDataType() : instance;
    }

    private final Set<String> enumClasses = new HashSet<>();
    private final Map<String, Function<String, String>> converters = new HashMap<>();
    private final Map<String, Function<String, String>> parsers = new HashMap<>();

    CacheDataType addEnumClasses(Set<String> enums) {
        enums.forEach(clazz -> addDataType(clazz, null, clazz + ".valueOf(json.getString(\"%s\"))"));
        return this;
    }

    CacheDataType addDataType(Set<CustomDataType> dataTypes) {
        if (Objects.nonNull(dataTypes)) {
            dataTypes.forEach(dt -> addDataType(dt.getClassName(), dt.getConverter(), dt.getParser()));
        }
        return this;
    }

    private void addDataType(String customClass, String convertCommand, String parseCommand) {
        String className = requireNotBlank(customClass, "Class cannot be blank");
        converters.put(className, Objects.isNull(convertCommand) ? Function.identity() : toFunc(convertCommand));
        parsers.put(className, Objects.isNull(parseCommand) ? Function.identity() : toFunc(parseCommand));
    }

    private Function<String, String> toFunc(String command) {
        return s -> String.format(requireNotBlank(command, "Command cannot be blank"), s);
    }

    private static String requireNotBlank(String text, String message) {
        if (text == null || "" .equals(text.trim())) {
            throw new IllegalArgumentException(message);
        }
        return text.trim();
    }

}
