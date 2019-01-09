package com.nubeiot.edge.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.search.LocalServiceSearch;

import lombok.Getter;
import lombok.NonNull;

public final class ModuleEventHandler implements EventHandler {

    private final EdgeVerticle verticle;
    @Getter
    private final List<EventAction> availableEvents;

    public ModuleEventHandler(@NonNull EdgeVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(events = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        JsonObject filter = data.getFilter();
        if (filter.getBoolean("available", Boolean.FALSE)) {
            return Single.just(new JsonObject());
        }
        return new LocalServiceSearch(this.verticle.getEntityHandler()).search(data);
    }

    @EventContractor(events = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        ITblModule module = new TblModule(data.getBody());
        if (Strings.isBlank(module.getServiceId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .findModuleById(module.getServiceId())
                            .map(o -> o.orElseThrow(() -> new NotFoundException(
                                String.format("Not found service id '%s'", module.getServiceId()))));
    }

    @EventContractor(events = EventAction.HALT, returnType = Single.class)
    public Single<JsonObject> halt(RequestData data) {
        ITblModule module = new TblModule(data.getBody());
        if (Strings.isBlank(module.getServiceId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .processDeploymentTransaction(new TblModule().setServiceId(module.getServiceId()),
                                                          EventAction.HALT);
    }

    @EventContractor(events = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        JsonObject body = data.getBody();
        ITblModule module = new TblModule().fromJson(body);
        if (Strings.isBlank(module.getServiceId())) {
            ModuleType moduleType = module.getServiceType();
            JsonObject moduleJson = moduleType.serialize(body, verticle.getModuleRule());
            module = module.fromJson(moduleJson);
            if (Strings.isBlank(module.getServiceName())) {
                throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT,
                                        "Provide at least service_id or service_name");
            }
            return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.UPDATE);
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.UPDATE);
    }

    @EventContractor(events = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> remove(RequestData data) {
        ITblModule module = new TblModule().fromJson(data.getBody());
        if (Strings.isBlank(module.getServiceId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.REMOVE);
    }

    @EventContractor(events = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData data) {
        JsonObject body = data.getBody();
        ModuleType moduleType = new TblModule().fromJson(body).getServiceType();
        JsonObject moduleJson = moduleType.serialize(body, this.verticle.getModuleRule());
        ITblModule module = new TblModule().fromJson(moduleJson);
        if (Strings.isBlank(module.getServiceName())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Missing service_name");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.CREATE);
    }

}
