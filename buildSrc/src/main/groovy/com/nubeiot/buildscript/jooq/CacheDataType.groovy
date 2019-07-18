package com.nubeiot.buildscript.jooq

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

import com.nubeiot.buildscript.Strings
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
    private final Map<String, String> defaultValues = new HashMap<>()
    private final Map<String, String> renameFields = new ConcurrentHashMap<>()

    Set<JsonDataType> getDataTypes() { return dataTypes }

    Function<String, String> getParser(String className) {
        return parsers.get(className)
    }

    Function<String, String> getConverter(String className) {
        return converters.get(className)
    }

    String getDefaultValue(String className) {
        return defaultValues.getOrDefault(className, "null")
    }

    String fieldName(String fieldName) {
        String[] out = [fieldName]
        renameFields.each { out[0] = out[0].replaceAll(it.key, it.value) }
        return out[0]
    }

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
            addDataType(dataType.className, dataType.converter, dataType.parser, dataType.defVal)
        }
        return this
    }

    CacheDataType addRenameFields(Map<String, String> renames) {
        renames.findAll { !Strings.isBlank(it.key) && Objects.nonNull(it.value) }.each {
            renameFields.put(Strings.toRegexIgnoreCase(it.key), it.value)
        }
        return this
    }

    def addDataType(String customClass, String convertCommand, String parseCommand, String defaultValue = "null") {
        String className = Strings.requireNotBlank(customClass, "Class cannot be blank")
        converters.put(className, toFunc(convertCommand))
        parsers.put(className, toFunc(parseCommand))
        defaultValues.put(className, Strings.isBlank(defaultValue) ? "null" : defaultValue)
    }

    private static Function<String, String> toFunc(String command) {
        if (Strings.isBlank(command)) {
            return Function.identity()
        }
        return { s -> String.format(command, s) }
    }

}
