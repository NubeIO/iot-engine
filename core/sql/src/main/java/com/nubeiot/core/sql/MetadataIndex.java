package com.nubeiot.core.sql;

import java.util.List;
import java.util.Optional;

import org.jooq.Table;

public interface MetadataIndex {

    List<EntityMetadata> index();

    default Optional<EntityMetadata> findByTable(Table table) {
        return index().stream().filter(metadata -> table.equals(metadata.table())).findFirst();
    }

}
