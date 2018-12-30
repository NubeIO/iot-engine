package com.nubeiot.buildscript.jooq;

import org.jooq.meta.TypedElementDefinition;

import io.github.jklingsporn.vertx.jooq.generate.VertxGeneratorStrategy;

public class NubeGeneratorStrategy extends VertxGeneratorStrategy {

    private static final String REGEX_JSON_COLUMN = "_json(_array)?$";

    public String getJsonKeyName(TypedElementDefinition<?> column) {
        return replaceJsonSuffix(column.getName());
    }

    private static String replaceJsonSuffix(String name) {
        return name.replaceAll(REGEX_JSON_COLUMN, "");
    }

}
