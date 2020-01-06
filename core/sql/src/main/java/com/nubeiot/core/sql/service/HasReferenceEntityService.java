package com.nubeiot.core.sql.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.marker.HasReferenceEntityMarker;
import com.nubeiot.core.utils.Functions;

import lombok.NonNull;

abstract class HasReferenceEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends AbstractEntityService<P, M> implements HasReferenceEntityMarker {

    HasReferenceEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    protected RequestData recomputeRequestData(@NonNull RequestData requestData, JsonObject extra) {
        JsonObject body = Optional.ofNullable(requestData.body()).orElseGet(JsonObject::new);
        JsonObject filter = new JsonObject(referencedEntities().computeRequest(body));
        Optional.ofNullable(extra).ifPresent(e -> filter.mergeIn(e, true));
        body = body.mergeIn(filter, true);
        final JsonObject combineFilter = Objects.isNull(requestData.filter())
                                         ? filter
                                         : requestData.filter().mergeIn(filter, true);
        return RequestData.builder()
                          .body(body)
                          .headers(requestData.headers())
                          .filter(combineFilter)
                          .sort(requestData.sort())
                          .pagination(requestData.pagination())
                          .build();
    }

    JsonObject convertKey(@NonNull RequestData reqData, @NonNull Stream<Entry<EntityMetadata, String>> ref) {
        JsonObject body = reqData.body();
        JsonObject extra = new JsonObject();
        ref.filter(entry -> !body.containsKey(entry.getKey().singularKeyName()) && body.containsKey(entry.getValue()))
           .forEach(entry -> extra.put(entry.getKey().singularKeyName(),
                                       new JsonObject().put(entry.getKey().jsonKeyName(),
                                                            body.getValue(entry.getValue()))));
        return extra;
    }

    JsonObject convertKey(@NonNull RequestData reqData, EntityMetadata... metadata) {
        return convertKey(reqData, Arrays.asList(metadata));
    }

    JsonObject convertKey(@NonNull RequestData reqData, List<EntityMetadata> metadata) {
        JsonObject object = new JsonObject();
        Function<EntityMetadata, Object> valFunc = meta -> JsonData.checkAndConvert(meta.parseKey(reqData));
        Function<EntityMetadata, String> keyFunc = meta -> context().requestKeyName().equals(meta.requestKeyName())
                                                           ? context().jsonKeyName()
                                                           : meta.requestKeyName();
        metadata.stream()
                .filter(Objects::nonNull)
                .map(meta -> new SimpleEntry<>(keyFunc.apply(meta),
                                               Functions.getIfThrow(() -> valFunc.apply(meta)).orElse(null)))
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .forEach(entry -> object.put(entry.getKey(), entry.getValue()));
        return object;
    }

}
