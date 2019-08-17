package com.nubeiot.core.sql.service;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.jooq.Record;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.validation.CompositeValidation;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} has one or more {@code reference} to other resources. It presents
 * many-to-one relationship.
 *
 * @param <M> Composite Metadata Type
 * @param <V> Composite Entity Validation
 */
@SuppressWarnings("unchecked")
public interface ManyToOneReferenceEntityService<M extends CompositeMetadata, V extends CompositeValidation>
    extends OneToManyReferenceEntityService<M, V> {

    <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> EntityMetadata<K, P, R, D> reference();

    @Override
    @NonNull M metadata();

    @Override
    @NonNull V validation();

    @Override
    default Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_LIST, requestData);
        return queryExecutor().from(metadata().table())
                              .with(reference().table())
                              .mapper(this::mapper)
                              .findMany(reqData)
                              .map(m -> transformer().afterEachList(m, reqData))
                              .collect(JsonArray::new, JsonArray::add)
                              .map(results -> new JsonObject().put(metadata().pluralKeyName(), results));
    }

    @Override
    default Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_ONE, requestData);
        return queryExecutor().from(metadata().table())
                              .with(reference().table())
                              .mapper(this::mapper)
                              .findOne(reqData)
                              .switchIfEmpty(Maybe.error(metadata().notFound(metadata().parsePrimaryKey(requestData))))
                              .onErrorResumeNext(ReferenceQueryExecutor::wrapDatabaseError)
                              .toSingle()
                              .map(pojo -> transformer().afterGet(pojo, reqData));
    }

    @Override
    default Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = recompute(EventAction.CREATE, requestData);
        final CompositePojo pojo = metadata().parse(reqData.body());
        final VertxPojo ref = pojo.safeGet(reference().singularKeyName(), reference().modelClass());
        final ComplexQueryExecutor from = queryExecutor().from(metadata().daoClass());
        final CompositePojo validated = validation().onCreate(pojo, reqData.headers());
        if (Objects.isNull(ref)) {
            Object refKey = reference().parsePrimaryKey(requestData);
            return reference().dao(entityHandler())
                              .findOneById(refKey)
                              .flatMap(o -> o.map(rf -> from.insertReturningPrimary(validated)
                                                            .flatMap(k -> cudResponse(EventAction.CREATE, k, reqData)))
                                             .orElseThrow(() -> reference().notFound(refKey)));
        }
        return from.insertReturningPrimary(validated).flatMap(k -> cudResponse(EventAction.CREATE, k, reqData));
    }

    @Override
    @NonNull ManyToOneEntityTransformer transformer();

    @Override
    default @NonNull ComplexQueryExecutor queryExecutor() {
        return entityHandler().complexQuery();
    }

    @Override
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        final @NonNull Object value = metadata().parsePrimaryKey(requestData);
        return recompute(requestData, new JsonObject().put(metadata().jsonKeyName(), JsonData.checkAndConvert(value)));
    }

    default CompositePojo mapper(Record r) {
        return ((CompositePojo) r.into(metadata().modelClass())).wrap(
            Collections.singletonMap(reference().singularKeyName(), r.into(reference().modelClass())));
    }

    interface ManyToOneEntityTransformer extends ReferenceEntityTransformer {

        CompositeMetadata metadata();

        EntityMetadata reference();

        @Override
        default Set<String> ignoreFields(@NonNull RequestData requestData) {
            return Stream.of(metadata().requestKeyName(), reference().requestKeyName())
                         .collect(() -> ReferenceEntityTransformer.super.ignoreFields(requestData), Set::add,
                                  Set::addAll);
        }

    }

}
