package com.nubeiot.buildscript.jooq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jooq.codegen.JavaWriter;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.TypedElementDefinition;

import io.github.jklingsporn.vertx.jooq.generate.builder.DIStep;
import io.github.jklingsporn.vertx.jooq.generate.builder.DelegatingVertxGenerator;
import io.github.jklingsporn.vertx.jooq.generate.builder.VertxGeneratorBuilder;

import com.nubeiot.buildscript.Strings;

public class NubeJdbcGenerator extends DelegatingVertxGenerator {

    public NubeJdbcGenerator() {
        this(VertxGeneratorBuilder.init().withRXAPI().withJDBCDriver());
    }

    NubeJdbcGenerator(DIStep step) {
        super(step.build());
    }

    private static boolean writeSetter(JavaWriter out, String setter, String javaMemberName, Function<String, String> f,
                                       String defVal) {
        String parser = f.apply(String.format("json.getValue(\"%s\")", javaMemberName));
        out.tab(2).println("%s(json.getValue(\"%s\")==null?%s:%s);", setter, javaMemberName, defVal, parser);
        return true;
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

    @Override
    protected void generateInterface(TableDefinition table) {
        super.generateInterface(table);
    }

    @Override
    protected void generateTableClassFooter(TableDefinition table, JavaWriter out) {
        out.println();
        out.tab(1).println("private final Map<String, String> jsonFields = Collections.unmodifiableMap(initFields());");
        out.println();
        out.tab(1).println("private Map<String, String> initFields() {");
        out.tab(2).println("Map<String, String> map = new HashMap();");
        table.getColumns().forEach(c -> {
            String jsonField = Strings.toSnakeCase(CacheDataType.instance().fieldName(c.getName()), false);
            out.tab(2).println("map.put(\"" + jsonField + "\", \"" + c.getName() + "\");");
        });
        out.tab(2).println("return map;");
        out.tab(1).println("}");
        out.println();
        out.tab(1).override();
        out.tab(1).println("public Map<String, String> jsonFields() {");
        out.tab(2).println("return jsonFields;");
        out.tab(1).println("}");
        out.println();
        out.ref(Map.class);
        out.ref(HashMap.class);
        out.ref(Collections.class);
    }

}
