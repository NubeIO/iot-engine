package com.nubeiot.buildscript.jooq


import java.util.function.Function

import com.nubeiot.buildscript.jooq.JooqGenerateTask.JsonDataType

class CacheDataType {

    private static CacheDataType instance

    private CacheDataType() {}

    static synchronized CacheDataType instance() {
        return instance == null ? instance = new CacheDataType() : instance
    }

    private final Set<JsonDataType> dataTypes = new HashSet<>()
    private final Map<String, Function<String, String>> converters = new HashMap<>()
    private final Map<String, Function<String, String>> parsers = new HashMap<>()

    Set<JsonDataType> getDataTypes() { return dataTypes }

    Map<String, Function<String, String>> getParsers() { return parsers }

    Map<String, Function<String, String>> getConverters() { return converters }

    CacheDataType addEnumClasses(Set<String> enums) {
        enums.each { clazz ->
            addDataType(new JsonDataType(className: clazz, converter: null,
                                         parser: "${clazz}.valueOf(((String)%s).toUpperCase())"))
        }
        return this
    }

    CacheDataType addDataType(Set<JsonDataType> dataTypes) {
        if (Objects.nonNull(dataTypes)) {
            dataTypes.each { dt -> addDataType(dt) }
        }
        return this
    }

    CacheDataType addDataType(JsonDataType dataType) {
        if (Objects.nonNull(dataType)) {
            dataTypes.add(dataType)
            addDataType(dataType.getClassName(), dataType.getConverter(), dataType.getParser())
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
