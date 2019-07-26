package com.nubeiot.buildscript.jooq

import org.jooq.meta.Definition
import org.jooq.meta.TableDefinition
import org.jooq.meta.TypedElementDefinition
import org.jooq.tools.StringUtils

import io.github.jklingsporn.vertx.jooq.generate.VertxGeneratorStrategy
import com.nubeiot.buildscript.Strings

class NubeGeneratorStrategy extends VertxGeneratorStrategy {

    @Override
    List<String> getJavaClassImplements(Definition definition, Mode mode) {
        List<String> javaClassImplements = super.getJavaClassImplements(definition, mode)
        if (mode == Mode.INTERFACE || mode == Mode.POJO || mode == Mode.RECORD) {
            TableDefinition table = definition.database.getTable(definition.schema, definition.name)
            if (table.columns.find({ it.name.matches(DB.COL_REGEX.timeAudit) })) {
                javaClassImplements.add("com.nubeiot.core.sql.HasTimeAudit")
            }
        }
        if (mode == Mode.DEFAULT && definition instanceof TableDefinition) {
            //TODO very hacky to add RECORD
            String recordClass = StringUtils.toCamelCase(definition.name) + "Record"
            javaClassImplements.add("com.nubeiot.core.sql.JsonTable<" + recordClass + ">")
        }
        return javaClassImplements
    }

    @Override
    String getJavaIdentifier(Definition definition) {
        return Strings.toSnakeCase(CacheDataType.instance().fieldName(definition.getOutputName()))
    }

    String getJsonKeyName(TypedElementDefinition column) {
        return Strings.toSnakeCase(CacheDataType.instance().fieldName(column.getName()), false)
    }

    @Override
    String getJavaMemberName(Definition definition, Mode mode) {
        return StringUtils.toCamelCase(CacheDataType.instance().fieldName(definition.getOutputName()))
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
