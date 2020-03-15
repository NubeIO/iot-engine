package com.nubeiot.core.sql;

import java.util.Objects;

import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.TableField;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
public final class ReferenceEntityMetadata {

    @NonNull
    private final ForeignKey foreignKey;
    private final Table table;
    private final EntityMetadata metadata;

    public static ReferenceEntityMetadata create(@NonNull MetadataIndex index, @NonNull ForeignKey foreignKey) {
        return ReferenceEntityMetadata.builder()
                                      .foreignKey(foreignKey)
                                      .metadata(index.findByTable(foreignKey.getTable()).orElse(null))
                                      .build();
    }

    public boolean isValid() {
        return this.foreignKey.getFields().size() == 1 && (Objects.nonNull(metadata) || Objects.nonNull(table));
    }

    public TableField getField() {
        return (TableField) this.foreignKey.getFields().get(0);
    }

    public static final class Builder {

        public ReferenceEntityMetadata build() {
            return new ReferenceEntityMetadata(foreignKey, Objects.isNull(table) ? foreignKey.getTable() : table,
                                               metadata);
        }

    }

}
