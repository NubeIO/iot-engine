package com.nubeiot.buildscript.jooq;

import java.util.function.Function;

import org.jooq.codegen.JavaWriter;
import org.jooq.meta.TypedElementDefinition;

import io.github.jklingsporn.vertx.jooq.generate.builder.DIStep;
import io.github.jklingsporn.vertx.jooq.generate.builder.DelegatingVertxGenerator;
import io.github.jklingsporn.vertx.jooq.generate.builder.VertxGeneratorBuilder;

public class NubeJdbcGenerator extends DelegatingVertxGenerator {

    public NubeJdbcGenerator() {
        super(VertxGeneratorBuilder.init().withRXAPI().withJDBCDriver().build());
    }

    public NubeJdbcGenerator(DIStep step) {
        super(step.build());
    }

    @Override
    protected boolean handleCustomTypeFromJson(TypedElementDefinition<?> column, String setter, String columnType,
                                               String javaMemberName, JavaWriter out) {
        if (CacheDataType.instance().getParsers().containsKey(columnType)) {
            return writeSetter(out, setter, javaMemberName, CacheDataType.instance().getParsers().get(columnType));
        }
        return super.handleCustomTypeFromJson(column, setter, columnType, javaMemberName, out);
    }

    @Override
    protected boolean handleCustomTypeToJson(TypedElementDefinition<?> column, String getter, String columnType,
                                             String javaMemberName, JavaWriter out) {
        if (CacheDataType.instance().getConverters().containsKey(columnType)) {
            return writeGetter(out, column, getter, CacheDataType.instance().getConverters().get(columnType));
        }
        return super.handleCustomTypeToJson(column, getter, columnType, javaMemberName, out);
    }

    private boolean writeGetter(JavaWriter out, TypedElementDefinition<?> column, String getter,
                                Function<String, String> f) {
        String method = getter + "()";
        out.tab(2).println("json.put(\"%s\",%s==null?null:%s);", getJsonKeyName(column), method, f.apply(method));
        return true;
    }

    private boolean writeSetter(JavaWriter out, String setter, String javaMemberName, Function<String, String> f) {
        out.tab(2).println("%s(json.getValue(\"%s\")==null?null:%s);", setter, javaMemberName, f.apply(javaMemberName));
        return true;
    }

}
