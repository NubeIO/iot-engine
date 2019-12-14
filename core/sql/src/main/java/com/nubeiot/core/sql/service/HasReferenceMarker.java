package com.nubeiot.core.sql.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents for an {@code database entity} marker.
 * <p>
 * It manifests {@code resource entity} has one or more {@code reference} to other resources. In {@code database layer}
 * it can be understand as {@code one table} has reference fields (a.k.a {@code foreign key}) to other {@code tables}.
 * <p>
 * A marker declares a list of {@code reference fields} that are used for computing {@code request data} to filter
 * exactly request key then make {@code SQL query}
 *
 * @since 1.0.0
 */
public interface HasReferenceMarker {

    /**
     * Defines mapping between {@code json field} in {@code request body} and {@code resource field} as {@code foreign
     * key} in database
     *
     * @return entity references
     * @see EntityReferences
     * @since 1.0.0
     */
    @NonNull EntityReferences entityReferences();

    /**
     * Defines ignore fields in {@code response} based on {@code request data} and {@code reference fields}
     *
     * @return ignore fields
     * @since 1.0.0
     */
    @NonNull
    default Set<String> ignoreFields() {
        return entityReferences().ignoreFields();
    }

    /**
     * Represents mapping between {@code json field} and {@code resource field}.
     * <p>
     * In most case, {@code json field} will be similar to {@code resource field}. For examples:
     * <table summary="">
     *  <tr><th>Json Field</th><th>Resource field</th></tr>
     *  <tr><td>device_id</td><td>DEVICE_ID</td></tr>
     *  <tr><td>device_id</td><td>DEVICE</td></tr>
     *  <tr><td>device</td><td>DEVICE</td></tr>
     * </table>
     *
     * @since 1.0.0
     */
    @Getter
    @RequiredArgsConstructor
    class EntityReferences {

        private final Map<EntityMetadata, String> fields = new LinkedHashMap<>();

        /**
         * Add entity references.
         *
         * @param metadata the metadata
         * @return the entity references
         * @since 1.0.0
         */
        public EntityReferences add(@NonNull EntityMetadata metadata) {
            fields.put(metadata, metadata.requestKeyName());
            return this;
        }

        /**
         * Add entity references.
         *
         * @param metadata the metadata
         * @param fkField  the json foreign key field. If it is {@code blank}, it will fallback to {@link
         *                 EntityMetadata#requestKeyName()}
         * @return the entity references
         * @since 1.0.0
         */
        public EntityReferences add(@NonNull EntityMetadata metadata, String fkField) {
            fields.put(metadata, Strings.isBlank(fkField) ? metadata.requestKeyName() : fkField);
            return this;
        }

        Set<String> ignoreFields() {
            return Stream.concat(fields.keySet().stream().map(EntityMetadata::requestKeyName), fields.values().stream())
                         .collect(Collectors.toSet());
        }

        Map<String, Object> computeRequest(@NonNull JsonObject body) {
            if (body.isEmpty()) {
                return body.getMap();
            }
            return fields.entrySet()
                         .stream()
                         .filter(entry -> body.containsKey(entry.getKey().requestKeyName()))
                         .collect(HashMap::new, (map, entry) -> map.put(entry.getValue(), getValue(body, entry)),
                                  Map::putAll);
        }

        private Object getValue(@NonNull JsonObject body, Entry<EntityMetadata, String> entry) {
            final EntityMetadata metadata = entry.getKey();
            final Object value = body.getValue(metadata.requestKeyName());
            return Objects.isNull(value)
                   ? null
                   : Functions.getOrThrow(() -> JsonData.checkAndConvert(metadata.parseKey(value.toString())),
                                          t -> new IllegalArgumentException("Invalid " + metadata.requestKeyName(), t));
        }

    }

}
