package com.nubeiot.core.sql.service;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.validation.CompositeValidation;

import lombok.NonNull;

public abstract class AbstractManyToOneEntityService<M extends CompositeMetadata, V extends CompositeValidation>
    extends AbstractEntityService<M, V> implements ManyToOneReferenceEntityService<M, V> {

    public AbstractManyToOneEntityService(@NonNull AbstractEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        return ManyToOneReferenceEntityService.super.list(requestData);
    }

    @Override
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        return ManyToOneReferenceEntityService.super.get(requestData);
    }

    @Override
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return ManyToOneReferenceEntityService.super.create(requestData);
    }

    @Override
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return super.update(requestData);
    }

    @Override
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return super.patch(requestData);
    }

    @Override
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        return super.delete(requestData);
    }

}
