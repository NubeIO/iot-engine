package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.UpdatableRecord;
import org.jooq.exception.TooManyRowsException;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

public interface HasReferenceEntityService<KEY, MODEL extends VertxPojo, RECORD extends UpdatableRecord<RECORD>, DAO extends VertxDAO<RECORD, MODEL, KEY>>
    extends InternalEntityService<KEY, MODEL, RECORD, DAO>, HasReferenceResource {

    @Override
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        if (action == EventAction.GET_LIST) {
            return recompute(requestData, null);
        }
        if (action != EventAction.CREATE) {
            return recompute(requestData, Collections.singletonMap(jsonKeyName(), parsePrimaryKey(requestData)));
        }
        return InternalEntityService.super.recompute(action, requestData);
    }

    @Override
    default @NonNull JsonObject customizeGetItem(@NonNull MODEL pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(computeIgnoreFields(requestData));
    }

    @Override
    default @NonNull JsonObject customizeCreatedItem(@NonNull MODEL pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(JsonData.MAPPER, computeIgnoreFields(requestData));
    }

    @Override
    default @NonNull JsonObject customizeModifiedItem(@NonNull MODEL pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(computeIgnoreFields(requestData));
    }

    @Override
    @NonNull
    default JsonObject customizeDeletedItem(@NonNull MODEL pojo, @NonNull RequestData requestData) {
        return JsonPojo.from(pojo).toJson(JsonData.MAPPER, computeIgnoreFields(requestData));
    }

    @Override
    default Single<MODEL> doGetOne(RequestData requestData) {
        KEY pk = parsePrimaryKey(requestData);
        return get().queryExecutor()
                    .findOne(ctx -> query(ctx, requestData))
                    .map(o -> o.orElseThrow(() -> notFound(pk)))
                    .onErrorResumeNext(t -> Single.error(t instanceof TooManyRowsException ? new StateException(
                        "Query is not correct, the result contains more than one record", t) : t));
    }

    default Set<String> computeIgnoreFields(@NonNull RequestData requestData) {
        JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
        return Stream.of(requestData.hasAudit() ? Stream.<String>empty() : IGNORE_FIELDS.stream(),
                         jsonFieldConverter().keySet().stream().filter(s -> filter.fieldNames().contains(s)),
                         jsonRefFields().values().stream().filter(s -> filter.fieldNames().contains(s)))
                     .flatMap(s -> s)
                     .collect(Collectors.toSet());
    }

    default RequestData recompute(RequestData requestData, Map<String, ?> extra) {
        JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
        Optional.ofNullable(requestData.body())
                .ifPresent(body -> body.stream()
                                       .filter(entry -> this.jsonFieldConverter().containsKey(entry.getKey()))
                                       .forEach(entry -> filter.put(
                                           jsonRefFields().getOrDefault(entry.getKey(), entry.getKey()),
                                           Optional.ofNullable(entry.getValue())
                                                   .map(v -> jsonFieldConverter().get(entry.getKey())
                                                                                 .apply(v.toString()))
                                                   .map(v -> ReflectionClass.isJavaLangObject(v.getClass())
                                                             ? v
                                                             : v.toString())
                                                   .orElse(null))));
        Optional.ofNullable(extra).ifPresent(m -> filter.getMap().putAll(m));
        return RequestData.builder()
                          .body(requestData.body())
                          .headers(requestData.headers())
                          .filter(filter)
                          .pagination(requestData.getPagination())
                          .build();
    }

}
