package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.List;
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

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.StateException;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} has one or more {@code reference} to other resources. It presents
 * many-to-one relationship.
 *
 * @param <KEY>    Primary key type
 * @param <POJO>   Pojo model type
 * @param <RECORD> Record type
 * @param <DAO>    DAO Type
 * @param <META>   Metadata Type
 * @param <CP>     Pojo composite type
 */
public interface ManyToOneReferenceEntityService<KEY, POJO extends VertxPojo, RECORD extends UpdatableRecord<RECORD>,
                                                    DAO extends VertxDAO<RECORD, POJO, KEY>, META extends CompositeEntityMetadata<KEY, POJO, RECORD, DAO, CP>, CP extends CompositePojo<POJO>>
    extends OneToManyReferenceEntityService<KEY, POJO, RECORD, DAO, META> {

    <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> EntityMetadata<K, P, R, D> reference();

    @Override
    @NonNull META metadata();

    @Override
    default Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_LIST, requestData);
        final Vertx vertx = entityHandler().getVertx();
        final Maybe<List<CP>> result = vertx.rxExecuteBlocking(
            h -> h.complete(queryView(reqData).fetch(this::viewMapper)));
        return result.flattenAsObservable(r -> r)
                     .map(m -> customizeEachItem(m.unwrap(), reqData))
                     .collect(JsonArray::new, JsonArray::add)
                     .map(results -> new JsonObject().put(metadata().pluralKeyName(), results));
    }

    @Override
    default Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_ONE, requestData);
        final Vertx vertx = entityHandler().getVertx();
        final Maybe<CP> result = vertx.rxExecuteBlocking(
            h -> h.complete(queryView(reqData).fetchOne(this::viewMapper)));
        KEY pk = parsePrimaryKey(requestData);
        return result.switchIfEmpty(Single.error(notFound(pk)))
                     .onErrorResumeNext(t -> Single.error(t instanceof TooManyRowsException ? new StateException(
                         "Query is not correct, the result contains more than one record", t) : t))
                     .map(pojo -> customizeGetItem(pojo.unwrap(), reqData));
    }

    @Override
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        return recompute(requestData, Collections.singletonMap(metadata().jsonKeyName(), parsePrimaryKey(requestData)));
    }

    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        return Stream.of(metadata().requestKeyName(), reference().requestKeyName())
                     .collect(() -> OneToManyReferenceEntityService.super.ignoreFields(requestData), Set::add,
                              Set::addAll);
    }

    @SuppressWarnings("unchecked")
    default CP viewMapper(Record r) {
        return (CP) r.into(metadata().compositeModelClass())
                     .wrap(Collections.singletonMap(reference().singularKeyName(), r.into(reference().modelClass())));
    }

    default ResultQuery<? extends Record> queryView(@NonNull RequestData requestData) {
        final DSLContext ctx = entityHandler().getJooqConfig().dsl();
        final Pagination paging = requestData.getPagination();
        return ctx.select()
                  .from(filter(ctx.selectFrom(metadata().table()).where(DSL.trueCondition()), requestData.getFilter()))
                  .join(reference().table())
                  .using(reference().table().getPrimaryKey().getFields())
                  .where(DSL.trueCondition())
                  .limit(paging.getPerPage())
                  .offset((paging.getPage() - 1) * paging.getPerPage());
    }

}
