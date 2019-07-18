package com.nubeiot.buildscript.jooq

import org.jooq.meta.Definition
import org.jooq.meta.TypedElementDefinition
import org.jooq.tools.StringUtils

import io.github.jklingsporn.vertx.jooq.generate.VertxGeneratorStrategy
import com.nubeiot.buildscript.Strings

class NubeGeneratorStrategy extends VertxGeneratorStrategy {

    @Override
    String getJavaIdentifier(Definition definition) {
        return CacheDataType.instance().fieldName(definition.getOutputName())
    }

    String getJsonKeyName(TypedElementDefinition column) {
        return Strings.toSnakeCase(CacheDataType.instance().fieldName(column.getName()), false)
    }

    @Override
    String getJavaMemberName(Definition definition, Mode mode) {
        return StringUtils.toCamelCaseLC(CacheDataType.instance().fieldName(definition.getOutputName()))
    }

    @Override
    String getJavaSetterName(Definition definition, Mode mode) {
        return "set" + StringUtils.toCamelCase(CacheDataType.instance().fieldName(definition.getOutputName()))
    }

    @Override
    String getJavaGetterName(Definition definition, Mode mode) {
        return "get" + StringUtils.toCamelCase(CacheDataType.instance().fieldName(definition.getOutputName()))
    }
}
