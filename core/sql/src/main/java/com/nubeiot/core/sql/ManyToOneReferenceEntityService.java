package com.nubeiot.core.sql;

import java.util.Collections;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;

import lombok.NonNull;

/**
 * Represents service that holds a {@code resource} has one or more {@code reference} to other resources. It presents
 * many-to-one relationship.
 *
 * @param <KEY>      Primary key type
 * @param <POJO>     Pojo model type
 * @param <RECORD>   Record type
 * @param <DAO>      DAO Type
 * @param <METADATA> Metadata Type
 */
public interface ManyToOneReferenceEntityService<KEY, POJO extends VertxPojo, RECORD extends UpdatableRecord<RECORD>,
                                                    DAO extends VertxDAO<RECORD, POJO, KEY>,
                                                    METADATA extends EntityMetadata<KEY, POJO, RECORD, DAO>>
    extends OneToManyReferenceEntityService<KEY, POJO, RECORD, DAO, METADATA> {

    EntityMetadata reference();

    @Override
    @NonNull
    default RequestData recompute(@NonNull EventAction action, @NonNull RequestData requestData) {
        return recompute(requestData, Collections.singletonMap(metadata().jsonKeyName(), parsePrimaryKey(requestData)));
    }

    @Override
    default Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_LIST, requestData);

        return doGetList(reqData).flatMapSingle(m -> Single.just(customizeEachItem(m, reqData)))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put(metadata().listKey(), results));
    }

    @Override
    default Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = recompute(EventAction.GET_ONE, requestData);
        return doGetOne(reqData).map(pojo -> customizeGetItem(pojo, reqData));
    }

}
