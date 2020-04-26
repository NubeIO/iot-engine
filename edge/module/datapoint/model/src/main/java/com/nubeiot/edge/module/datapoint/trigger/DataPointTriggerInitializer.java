package com.nubeiot.edge.module.datapoint.trigger;

import java.util.Arrays;
import java.util.List;

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
        final List<String> triggers = Arrays.asList(
            createTrigger("POINT_TRANSDUCER_CREATION_TRIGGER", "INSERT", Tables.POINT_TRANSDUCER,
                          PointTransducerTrigger.class),
            createTrigger("POINT_TRANSDUCER_MODIFICATION_TRIGGER", "UPDATE", Tables.POINT_TRANSDUCER,
                          PointTransducerTrigger.class),
            createTrigger("FOLDER_GROUP_CREATION_TRIGGER", "INSERT", Tables.FOLDER_GROUP, FolderGroupTrigger.class),
            createTrigger("FOLDER_GROUP_MODIFICATION_TRIGGER", "UPDATE", Tables.FOLDER_GROUP,
                          FolderGroupTrigger.class));
        return dsl.execute(String.join(";", triggers));
    }

}
