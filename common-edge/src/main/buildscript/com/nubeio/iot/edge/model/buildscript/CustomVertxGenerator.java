package com.nubeio.iot.edge.model.buildscript;

import java.util.Date;

import org.jooq.util.JavaWriter;
import org.jooq.util.TypedElementDefinition;

import com.nubeio.iot.edge.loader.ModuleType;
import com.nubeio.iot.share.enums.State;
import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.event.EventType;

import io.github.jklingsporn.vertx.jooq.generate.VertxGenerator;

public final class CustomVertxGenerator extends VertxGenerator {

    @Override
    protected boolean handleCustomTypeFromJson(TypedElementDefinition<?> column, String setter, String columnType,
                                               String javaMemberName, JavaWriter out) {
        if (isType(columnType, Date.class)) {
            out.tab(2)
               .println("%s(json.getLong(\"%s\")==null?null:Date.from(java.time.Instant.ofEpochMilli(json.getLong" +
                        "(\"%s\"))));", setter, javaMemberName, javaMemberName);
            return true;
        }
        if (isType(columnType, Status.class) || isType(columnType, State.class) ||
            isType(columnType, EventType.class)) {
            return writeSetterForEnum(setter, javaMemberName, out, columnType);
        }
        if (isType(columnType, ModuleType.class)) {
            out.tab(2)
               .println("%s(json.getString(\"%s\")==null?null:com.nubeio.iot.edge.loader.ModuleTypeFactory.factory" +
                        "(json" + ".getString(\"%s\")));", setter, javaMemberName, javaMemberName);
            return true;
        }
        return super.handleCustomTypeFromJson(column, setter, columnType, javaMemberName, out);
    }

    @Override
    protected boolean handleCustomTypeToJson(TypedElementDefinition<?> column, String getter, String columnType,
                                             String javaMemberName, JavaWriter out) {
        if (isType(columnType, Date.class)) {
            out.tab(2).println("json.put(\"%s\",%s()==null?null:%s().getTime());", getJsonKeyName(column), getter, getter);
            return true;
        }
        if (isType(columnType, Status.class) || isType(columnType, State.class) ||
            isType(columnType, EventType.class)) {
            out.tab(2).println("json.put(\"%s\",%s()==null?null:%s());", getJsonKeyName(column), getter, getter);
            return true;
        }
        if (isType(columnType, ModuleType.class)) {
            out.tab(2).println("json.put(\"%s\",%s()==null?null:%s().name());", getJsonKeyName(column), getter, getter);
            return true;
        }
        return super.handleCustomTypeToJson(column, getter, columnType, javaMemberName, out);
    }

    private boolean writeSetterForEnum(String setter, String javaMemberName, JavaWriter out, String className) {
        out.tab(2)
           .println("%s(json.getString(\"%s\")==null?null:%s.valueOf(json.getString(\"%s\")));", setter, javaMemberName,
                    className, javaMemberName);
        return true;
    }

}
