package com.nubeiot.edge.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
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

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        JsonObject filter = data.getFilter();
        if (filter.getBoolean("available", Boolean.FALSE)) {
            return Single.just(new JsonObject());
        }
        return new LocalServiceSearch(this.verticle.getEntityHandler()).search(data);
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        RequestedServiceData serviceData = JsonData.from(data.body(), RequestedServiceData.class);
        ITblModule module = verticle.getModuleRule().parse(serviceData.getMetadata());
        if (Strings.isBlank(module.getServiceId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .findModuleById(module.getServiceId())
                            .map(o -> o.orElseThrow(() -> new NotFoundException(
                                String.format("Not found service id '%s'", module.getServiceId()))));
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> updatePartly(RequestData data) {
        ITblModule module = createTblModule(data.body());
        if (Strings.isBlank(module.getServiceId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .processDeploymentTransaction(new TblModule().fromJson(data.body()), EventAction.PATCH);
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        JsonObject body = data.body();
        ITblModule module = createTblModule(body);

        if (Strings.isBlank(module.getServiceId())) {
            ModuleType moduleType = module.getServiceType();
            RequestedServiceData serviceData = JsonData.from(body, RequestedServiceData.class);
            JsonObject moduleJson = moduleType.serialize(serviceData.getMetadata(), verticle.getModuleRule());
            module = module.fromJson(moduleJson);
            if (Strings.isBlank(module.getServiceName())) {
                throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT,
                                        "Provide at least service_id or service_name");
            }
            return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.UPDATE);
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.UPDATE);
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> remove(RequestData data) {
        RequestedServiceData serviceData = JsonData.from(data.body(), RequestedServiceData.class);
        ITblModule module = verticle.getModuleRule().parse(serviceData.getMetadata());
        if (Strings.isBlank(module.getServiceId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.REMOVE);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData data) {
        ITblModule module = createTblModule(data.body());
        if (Strings.isBlank(module.getServiceName())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Missing service_name");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.CREATE);
    }

    private ITblModule createTblModule(JsonObject body) {
        RequestedServiceData serviceData = JsonData.from(body, RequestedServiceData.class);
        return verticle.getModuleRule()
                       .parse(SharedDataDelegate.getLocalDataValue(verticle.getVertx(), verticle.getSharedKey(),
                                                                   SharedDataDelegate.SHARED_DATADIR),
                              serviceData.getMetadata(),
                              Objects.isNull(serviceData.getAppConfig())
                              ? IConfig.from(new JsonObject(), AppConfig.class)
                              : serviceData.getAppConfig());
    }

}
