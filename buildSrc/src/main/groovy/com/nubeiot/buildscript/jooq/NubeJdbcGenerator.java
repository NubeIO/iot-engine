package com.nubeiot.buildscript.jooq;

import java.util.Objects;
import java.util.function.Function;

import org.jooq.codegen.JavaWriter;
import org.jooq.meta.TypedElementDefinition;

import io.github.jklingsporn.vertx.jooq.generate.builder.DIStep;
import io.github.jklingsporn.vertx.jooq.generate.builder.DelegatingVertxGenerator;
import io.github.jklingsporn.vertx.jooq.generate.builder.VertxGeneratorBuilder;

public class NubeJdbcGenerator extends DelegatingVertxGenerator {

    public NubeJdbcGenerator() {
        this(VertxGeneratorBuilder.init().withRXAPI().withJDBCDriver());
    }

    public NubeJdbcGenerator(DIStep step) {
        super(step.build());
    }

    @Override
    protected boolean handleCustomTypeFromJson(TypedElementDefinition column, String setter, String columnType,
                                               String javaMemberName, JavaWriter out) {
        Function<String, String> parser = CacheDataType.instance().getParser(columnType);
        if (Objects.nonNull(parser)) {
            return writeSetter(out, setter, javaMemberName, parser,
                               CacheDataType.instance().getDefaultValue(columnType));
        }
        return super.handleCustomTypeFromJson(column, setter, columnType, javaMemberName, out);
    }

    @Override
    protected boolean handleCustomTypeToJson(TypedElementDefinition column, String getter, String columnType,
                                             String javaMemberName, JavaWriter out) {
        Function<String, String> converter = CacheDataType.instance().getConverter(columnType);
        if (Objects.nonNull(converter)) {
            return writeGetter(out, column, getter, converter);
        }
        return super.handleCustomTypeToJson(column, getter, columnType, javaMemberName, out);
    }

    private boolean writeGetter(JavaWriter out, TypedElementDefinition column, String getter,
                                Function<String, String> f) {
        String method = getter + "()";
        out.tab(2).println("json.put(\"%s\",%s==null?null:%s);", getJsonKeyName(column), method, f.apply(method));
        return true;
    }

    private static boolean writeSetter(JavaWriter out, String setter, String javaMemberName, Function<String, String> f,
                                       String defVal) {
        String parser = f.apply(String.format("json.getValue(\"%s\")", javaMemberName));
        out.tab(2).println("%s(json.getValue(\"%s\")==null?%s:%s);", setter, javaMemberName, defVal, parser);
        return true;
    }

}
