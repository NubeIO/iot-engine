package com.nubeiot.buildscript.jooq

import org.jooq.meta.Definition
import org.jooq.meta.TypedElementDefinition
import org.jooq.tools.StringUtils

import io.github.jklingsporn.vertx.jooq.generate.VertxGeneratorStrategy

class NubeGeneratorStrategy extends VertxGeneratorStrategy {

    @Override
    String getJavaIdentifier(Definition definition) {
        return Utils.replaceJsonSuffix(definition.getOutputName())
    }

    String getJsonKeyName(TypedElementDefinition column) {
        return Utils.toSnakeCase(Utils.replaceJsonSuffix(column.getName()), false)
    }

    @Override
    String getJavaMemberName(Definition definition, Mode mode) {
        return StringUtils.toCamelCaseLC(Utils.replaceJsonSuffix(definition.getOutputName()))
    }

    @Override
    String getJavaSetterName(Definition definition, Mode mode) {
        return "set" + StringUtils.toCamelCase(Utils.replaceJsonSuffix(definition.getOutputName()))
    }

    @Override
    String getJavaGetterName(Definition definition, Mode mode) {
        return "get" + StringUtils.toCamelCase(Utils.replaceJsonSuffix(definition.getOutputName()))
    }
}