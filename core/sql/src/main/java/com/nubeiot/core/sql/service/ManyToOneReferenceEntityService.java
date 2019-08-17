package com.nubeiot.core.sql.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.UpdatableRecord;
import org.jooq.exception.TooManyRowsException;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.validation.CompositeValidation;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} has one or more {@code reference} to other resources. It presents
 * many-to-one relationship.
 *
 * @param <M> Metadata Type
 */
public interface ManyToOneReferenceEntityService<M extends CompositeMetadata, V extends CompositeValidation>
    extends OneToManyReferenceEntityService<M, V> {

    <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> EntityMetadata<K, P, R, D> reference();

    @Override
    @NonNull M metadata();

    @Override
    default Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_LIST, requestData);
        final Vertx vertx = entityHandler().getVertx();
        final Maybe<List<VertxPojo>> result = vertx.rxExecuteBlocking(
            h -> h.complete(queryView(reqData).fetch(this::viewMapper)));
        return result.flattenAsObservable(r -> r).map(m -> transformer().afterEachList(m, reqData))
                     .collect(JsonArray::new, JsonArray::add)
                     .map(results -> new JsonObject().put(metadata().pluralKeyName(), results));
    }

    @Override
    default Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_ONE, requestData);
        final Vertx vertx = entityHandler().getVertx();
        final Maybe<VertxPojo> result = vertx.rxExecuteBlocking(
            h -> h.complete(queryView(reqData).fetchOne(this::viewMapper)));
        Object pk = metadata().parsePrimaryKey(requestData);
        return result.switchIfEmpty(Single.error(metadata().notFound(pk)))
                     .onErrorResumeNext(t -> Single.error(t instanceof TooManyRowsException ? new StateException(
                         "Query is not correct, the result contains more than one record", t) : t))
                     .map(pojo -> transformer().afterGet(pojo, reqData));
    }

    @Override
    @SuppressWarnings("unchecked")
    default Single<JsonObject> create(RequestData requestData) {
        RequestData reqData = recompute(EventAction.CREATE, requestData);
        final CompositePojo pojo = metadata().parse(reqData.body());
        final VertxPojo ref = pojo.safeGet(reference().singularKeyName(), reference().modelClass());
        if (Objects.isNull(ref)) {
            Object refKey = reference().parsePrimaryKey(requestData);
            return reference().getDao(entityHandler())
                              .findOneById(refKey)
                              .flatMap(o -> o.map(
                                  rf -> dao().insertReturningPrimary(validation().onCreate(pojo, reqData.headers()))
                                             .flatMap(k -> cudResponse(EventAction.CREATE, k, reqData)))
                                             .orElseThrow(() -> reference().notFound(refKey)));
        }
        return dao().insertReturningPrimary(validation().onCreate(pojo, reqData.headers()))
                    .flatMap(k -> cudResponse(EventAction.CREATE, k, reqData));
    }

    @Override
    @NonNull ManyToOneEntityTransformer transformer();

    @Override
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        final @NonNull Object value = metadata().parsePrimaryKey(requestData);
        return recompute(requestData, new JsonObject().put(metadata().jsonKeyName(), JsonData.checkAndConvert(value)));
    }

    default CompositePojo viewMapper(Record r) {
        return ((CompositePojo) r.into(metadata().modelClass())).wrap(
            Collections.singletonMap(reference().singularKeyName(), r.into(reference().modelClass())));
    }

    @SuppressWarnings("unchecked")
    default ResultQuery<? extends Record> queryView(@NonNull RequestData requestData) {
        final DSLContext ctx = entityHandler().dsl();
        final Pagination paging = requestData.getPagination();
        return ctx.select()
                  .from(filter(ctx.selectFrom(metadata().table()).where(DSL.trueCondition()), requestData.getFilter()))
                  .join(reference().table())
                  .using(reference().table().getPrimaryKey().getFields())
                  .limit(paging.getPerPage())
                  .offset((paging.getPage() - 1) * paging.getPerPage());
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
