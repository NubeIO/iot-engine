package com.nubeiot.buildscript.jooq

import java.time.Instant
import java.util.function.Function

import com.nubeiot.buildscript.jooq.JooqGenerateTask.CustomDataType

class CacheDataType {
    private static CacheDataType instance

    private CacheDataType() {
        this.addDataType(Date.class.getName(), "%s.getTime()",
                         "Date.from(java.time.Instant.ofEpochMilli((Long)%s))")
        this.addDataType(Instant.class.getName(), "%s.toEpochMilli()",
                         "java.time.Instant.ofEpochMilli((Long)%s))")
    }

    static synchronized CacheDataType instance() {
        return instance == null ? instance = new CacheDataType() : instance
    }

    private final Map<String, Function<String, String>> converters = new HashMap<>()
    private final Map<String, Function<String, String>> parsers = new HashMap<>()

    Map<String, Function<String, String>> getParsers() { return parsers }

    Map<String, Function<String, String>> getConverters() { return converters }

    CacheDataType addEnumClasses(Set<String> enums) {
        enums.each { clazz -> addDataType(clazz, null, clazz + ".valueOf((String)%s)") }
        return this
    }

    CacheDataType addDataType(Set<CustomDataType> dataTypes) {
        if (Objects.nonNull(dataTypes)) {
            dataTypes.each { dt -> addDataType(dt.getClassName(), dt.getConverter(), dt.getParser()) }
        }
        return this
    }

    def addDataType(String customClass, String convertCommand, String parseCommand) {
        String className = Utils.requireNotBlank(customClass, "Class cannot be blank")
        converters.put(className, Objects.isNull(convertCommand) ? Function.identity() : toFunc(convertCommand))
        parsers.put(className, Objects.isNull(parseCommand) ? Function.identity() : toFunc(parseCommand))
    }

    private static Function<String, String> toFunc(String command) {
        return { s -> String.format(Utils.requireNotBlank(command, "Command cannot be blank"), s) }
    }

}
