package com.nubeiot.core.sql.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} has one or more {@code reference} to other resources. It presents
 * one-to-one or one-to-many relationship.
 *
 * @param <M> Metadata Type
 */
@SuppressWarnings("unchecked")
public interface OneToManyReferenceEntityService<M extends EntityMetadata, V extends EntityValidation>
    extends InternalEntityService<M, V>, HasReferenceResource {

    @Override
    @NonNull ReferenceEntityTransformer transformer();

    @Override
    default @NonNull ReferenceQueryExecutor queryExecutor() {
        return ReferenceQueryExecutor.create(entityHandler(), metadata());
    }

    @Override
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        if (action == EventAction.GET_LIST) {
            return recompute(requestData, null);
        }
        if (action != EventAction.CREATE) {
            return recompute(requestData, new JsonObject().put(metadata().jsonKeyName(), JsonData.checkAndConvert(
                metadata().parsePrimaryKey(requestData))));
        }
        return InternalEntityService.super.recompute(action, requestData);
    }

    default RequestData recompute(@NonNull RequestData requestData, JsonObject extra) {
        JsonObject body = Optional.ofNullable(requestData.body()).orElseGet(JsonObject::new);
        Map<String, Object> f = body.stream()
                                    .filter(e -> jsonFieldConverter().containsKey(e.getKey()))
                                    .collect(HashMap::new,
                                             (m, v) -> m.put(keyMapper().apply(v), valueMapper().apply(v)),
                                             Map::putAll);
        Optional.ofNullable(extra).ifPresent(e -> f.putAll(e.getMap()));
        JsonObject filter = new JsonObject(f);
        body = body.mergeIn(filter, true);
        final JsonObject combineFilter = Objects.isNull(requestData.getFilter())
                                         ? filter
                                         : requestData.getFilter().mergeIn(filter, true);
        return RequestData.builder()
                          .body(body)
                          .headers(requestData.headers())
                          .filter(combineFilter)
                          .pagination(requestData.getPagination())
                          .build();
    }

    interface ReferenceEntityTransformer extends EntityTransformer {

        HasReferenceResource ref();

        @Override
        default Set<String> ignoreFields(@NonNull RequestData requestData) {
            JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
            return Stream.of(EntityTransformer.super.ignoreFields(requestData).stream(),
                             ref().jsonFieldConverter().keySet().stream().filter(s -> filter.fieldNames().contains(s)),
                             ref().jsonRefFields().values().stream().filter(s -> filter.fieldNames().contains(s)))
                         .flatMap(s -> s)
                         .collect(Collectors.toSet());
        }

    }

}
