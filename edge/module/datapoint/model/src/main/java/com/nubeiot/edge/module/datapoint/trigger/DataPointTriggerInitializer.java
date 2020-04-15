package com.nubeiot.edge.module.datapoint.trigger;

import java.util.Arrays;

import org.h2.api.Trigger;
import org.jooq.DSLContext;
import org.jooq.Table;

import com.nubeiot.iotdata.edge.model.Tables;

import lombok.NonNull;

public final class DataPointTriggerInitializer {

    private static String createTrigger(@NonNull String name, @NonNull String action, @NonNull Table table,
                                        @NonNull Class<? extends Trigger> clazz) {
        return "CREATE TRIGGER IF NOT EXISTS " + name + " BEFORE " + action + " ON " + table.getName() +
               " FOR EACH ROW CALL \"" + clazz.getName() + "\"";
    }

    public int execute(@NonNull DSLContext dsl) {
        return dsl.execute(String.join(";", Arrays.asList(
            createTrigger("POINT_TRANSDUCER_CREATION_TRIGGER", "INSERT", Tables.POINT_TRANSDUCER,
                          PointTransducerTrigger.class),
            createTrigger("POINT_TRANSDUCER_MODIFICATION_TRIGGER", "UPDATE", Tables.POINT_TRANSDUCER,
                          PointTransducerTrigger.class))));
    }

}
