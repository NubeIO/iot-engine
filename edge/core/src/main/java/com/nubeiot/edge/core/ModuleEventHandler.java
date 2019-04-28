package com.nubeiot.edge.core;

import java.nio.file.Path;
import java.nio.file.Paths;
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
        String serviceId = data.body().getString("service_id");
        if (Strings.isBlank(serviceId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .findModuleById(serviceId)
                            .map(o -> o.orElseThrow(
                                () -> new NotFoundException(String.format("Not found service id '%s'", serviceId))));
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> updatePartly(RequestData data) {
        ITblModule module = createTblModule(data.body());
        if (Strings.isBlank(module.getServiceId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.PATCH);
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        ITblModule module = createTblModule(data.body());

        if (Strings.isBlank(module.getServiceName()) && Strings.isBlank(module.getServiceId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT,
                                    "Provide at least service_id or service_name");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.UPDATE);
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> remove(RequestData data) {
        ITblModule module = new TblModule().setServiceId(data.body().getString("service_id"));
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
        String serviceId = body.getString("service_id");
        body.remove("service_id");
        RequestedServiceData serviceData = body.isEmpty()
                                           ? new RequestedServiceData()
                                           : JsonData.from(body, RequestedServiceData.class);
        if (Objects.nonNull(serviceId)) {
            serviceData.getMetadata().put("service_id", serviceId);
        }
        return createTblModule(serviceData.getMetadata(), serviceData.getAppConfig());
    }

    private ITblModule createTblModule(JsonObject metaData, AppConfig config) {
        return verticle.getModuleRule().parse(getDataDir(), metaData, getAppConfig(config));
    }

    private AppConfig getAppConfig(AppConfig appConfig) {
        return Objects.isNull(appConfig) ? IConfig.from(new JsonObject(), AppConfig.class) : appConfig;
    }

    private Path getDataDir() {
        String dataDir = SharedDataDelegate.getLocalDataValue(verticle.getVertx(), verticle.getSharedKey(),
                                                              SharedDataDelegate.SHARED_DATADIR);
        return Paths.get(dataDir);
    }

}
