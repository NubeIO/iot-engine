package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

import lombok.NonNull;

public interface ExtensionEntityService<KEY, MODEL extends VertxPojo, RECORD extends UpdatableRecord<RECORD>,
                                           DAO extends VertxDAO<RECORD, MODEL, KEY>>
    extends InternalEntityService<KEY, MODEL, RECORD, DAO>, ExtensionResource {

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
        final Set<String> ignores = new HashSet<>();
        ignores.addAll(requestData.hasAudit() ? Collections.emptySet() : IGNORE_FIELDS);
        ignores.addAll(
            extensions().keySet().stream().filter(s -> filter.fieldNames().contains(s)).collect(Collectors.toSet()));
        return ignores;
    }

    default RequestData recompute(RequestData requestData, Map<String, ?> extra) {
        JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
        Optional.ofNullable(requestData.body())
                .ifPresent(body -> body.stream()
                                       .filter(entry -> this.extensions().containsKey(entry.getKey()))
                                       .forEach(entry -> filter.put(entry.getKey(), this.extensions()
                                                                                        .get(entry.getKey())
                                                                                        .apply(entry.getValue()
                                                                                                    .toString()))));
        Optional.ofNullable(extra).ifPresent(m -> filter.getMap().putAll(m));
        return RequestData.builder()
                          .body(requestData.body())
                          .headers(requestData.headers())
                          .filter(filter)
                          .pagination(requestData.getPagination())
                          .build();
    }

}
