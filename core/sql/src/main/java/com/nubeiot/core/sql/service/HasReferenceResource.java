package com.nubeiot.core.sql.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Mark {@code EntityService} as representing {@code resource} has one or more {@code reference} to other resources. It
 * makes {@code EntityService} is available in a case that is lookup {@code current entity} from another reference
 * entity.
 * <p>
 * It contains reference fields (a.k.a {@code foreign key} to other table of this entity) that are used for computing
 * {@code request data} to filter exactly request key then make {@code sql query} in persistence layer
 *
 * @see EntityService
 */
public interface HasReferenceResource {

    /**
     * Defines mapping between {@code json request field} in {@code request body} and {@code database foreign key}
     *
     * @return a mapping between {@code json request field} and {@code database foreign key}
     */
    EntityReferences entityReferences();

    default Set<String> ignoreFields() {
        return entityReferences().ignoreFields();
    }

    @Getter
    @RequiredArgsConstructor
    class EntityReferences {

        private final Map<EntityMetadata, String> fields = new HashMap<>();

        public EntityReferences add(@NonNull EntityMetadata metadata) {
            fields.put(metadata, metadata.requestKeyName());
            return this;
        }

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
            final Object value = body.getValue(entry.getKey().requestKeyName());
            return Objects.isNull(value) ? null : JsonData.checkAndConvert(entry.getKey().parseKey(value.toString()));
        }

    }

}
