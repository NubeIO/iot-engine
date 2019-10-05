package com.nubeiot.edge.core.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.RequestedServiceData;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.search.LocalServiceSearch;

import lombok.NonNull;

public abstract class ModuleService implements InstallerService {

    @NonNull
    private final InstallerVerticle verticle;

    protected ModuleService(@NonNull InstallerVerticle verticle) {
        this.verticle = verticle;
    }

    public final String servicePath() {
        return "";
    }

    public final String paramPath() {
        return "service_id";
    }

    @Override
    public final @NonNull Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.CRUD_MAP.get().keySet();
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        JsonObject filter = data.getFilter();
        if (filter.getBoolean("available", Boolean.FALSE)) {
            return Single.just(new JsonObject());
        }
        return new LocalServiceSearch(verticle.getEntityHandler()).search(data);
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        String serviceId = data.body().getString(paramPath());
        if (Strings.isBlank(serviceId)) {
            throw new IllegalArgumentException("Service id is mandatory");
        }
        return verticle.getEntityHandler()
                       .findModuleById(serviceId)
                       .map(o -> o.map(this::removeCredentialsInAppConfig))
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .switchIfEmpty(Single.error(new NotFoundException("Not found service id '" + serviceId + "'")));
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData data) {
        ITblModule module = createTblModule(data.body());
        if (Strings.isBlank(module.getServiceId())) {
            throw new IllegalArgumentException("Service id is mandatory");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.PATCH);
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        ITblModule module = validate(data.body());
        if (Strings.isBlank(module.getServiceName()) && Strings.isBlank(module.getServiceId())) {
            throw new IllegalArgumentException("Provide at least service id or service name");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.UPDATE);
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> remove(RequestData data) {
        ITblModule module = new TblModule().setServiceId(data.body().getString(paramPath()));
        if (Strings.isBlank(module.getServiceId())) {
            throw new IllegalArgumentException("Service id is mandatory");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.REMOVE);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData data) {
        return this.verticle.getEntityHandler().processDeploymentTransaction(validate(data.body()), EventAction.CREATE);
    }

    private JsonObject removeCredentialsInAppConfig(TblModule record) {
        record.setAppConfig(
            verticle.getEntityHandler().getSecureAppConfig(record.getServiceId(), record.getAppConfig()));
        return record.toJson();
    }

    private ITblModule validate(@NonNull JsonObject body) {
        ITblModule module = createTblModule(body);
        if (Strings.isBlank(module.getServiceName())) {
            throw new IllegalArgumentException("Service name is mandatory");
        }
        if (Strings.isBlank(module.getVersion())) {
            throw new IllegalArgumentException("Service version is mandatory");
        }
        return module;
    }

    private ITblModule createTblModule(JsonObject body) {
        String serviceId = body.getString(paramPath());
        body.remove(paramPath());
        RequestedServiceData serviceData = body.isEmpty()
                                           ? new RequestedServiceData()
                                           : JsonData.from(body, RequestedServiceData.class);
        if (Strings.isNotBlank(serviceId)) {
            serviceData.getMetadata().put(paramPath(), serviceId);
        }
        return verticle.getModuleRule().parse(getDataDir(), serviceData.getMetadata(), serviceData.getAppConfig());
    }

    private Path getDataDir() {
        final String dataDir = verticle.getEntityHandler().sharedData(SharedDataDelegate.SHARED_DATADIR);
        return Strings.isBlank(dataDir) ? FileUtils.DEFAULT_DATADIR : Paths.get(dataDir);
    }

}
