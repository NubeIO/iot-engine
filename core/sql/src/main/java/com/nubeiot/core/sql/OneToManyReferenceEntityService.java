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

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} has one or more {@code reference} to other resources. It presents
 * one-to-one or one-to-many relationship.
 *
 * @param <KEY>      Primary key type
 * @param <POJO>     Pojo model type
 * @param <RECORD>   Record type
 * @param <DAO>      DAO Type
 * @param <METADATA> Metadata Type
 */
public interface OneToManyReferenceEntityService<KEY, POJO extends VertxPojo, RECORD extends UpdatableRecord<RECORD>,
                                                    DAO extends VertxDAO<RECORD, POJO, KEY>,
                                                    METADATA extends EntityMetadata<KEY, POJO, RECORD, DAO>>
    extends InternalEntityService<KEY, POJO, RECORD, DAO, METADATA>, HasReferenceResource {

    @Override
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        if (action == EventAction.GET_LIST) {
            return recompute(requestData, null);
        }
        if (action != EventAction.CREATE) {
            return recompute(requestData,
                             Collections.singletonMap(metadata().jsonKeyName(), parsePrimaryKey(requestData)));
        }
        return InternalEntityService.super.recompute(action, requestData);
    }

    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
        return Stream.of(InternalEntityService.super.ignoreFields(requestData).stream(),
                         jsonFieldConverter().keySet().stream().filter(s -> filter.fieldNames().contains(s)),
                         jsonRefFields().values().stream().filter(s -> filter.fieldNames().contains(s)))
                     .flatMap(s -> s)
                     .collect(Collectors.toSet());
    }

    @Override
    default Single<POJO> doGetOne(RequestData requestData) {
        KEY pk = parsePrimaryKey(requestData);
        return get().queryExecutor()
                    .findOne(ctx -> query(ctx, requestData))
                    .map(o -> o.orElseThrow(() -> notFound(pk)))
                    .onErrorResumeNext(t -> Single.error(t instanceof TooManyRowsException ? new StateException(
                        "Query is not correct, the result contains more than one record", t) : t));
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
