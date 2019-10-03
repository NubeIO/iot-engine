package com.nubeiot.edge.core.service;

import static com.nubeiot.core.http.base.event.ActionMethodMapping.defaultCRUDMap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.RequestedServiceData;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.search.LocalServiceSearch;

import lombok.NonNull;

public abstract class ModuleService implements InstallerService {

    public static final String SERVICE_ID_KEY = "service_id";
    @NonNull
    private final EdgeVerticle verticle;

    public ModuleService(@NonNull EdgeVerticle verticle) {
        this.verticle = verticle;
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
        String serviceId = data.body().getString("service_id");
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

    private JsonObject removeCredentialsInAppConfig(TblModule record) {
        record.setAppConfig(
            verticle.getEntityHandler().getSecureAppConfig(record.getServiceId(), record.getAppConfig()));
        return record.toJson();
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
        ITblModule module = new TblModule().setServiceId(data.body().getString("service_id"));
        if (Strings.isBlank(module.getServiceId())) {
            throw new IllegalArgumentException("Service id is mandatory");
        }
        return this.verticle.getEntityHandler().processDeploymentTransaction(module, EventAction.REMOVE);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData data) {
        return this.verticle.getEntityHandler().processDeploymentTransaction(validate(data.body()), EventAction.CREATE);
    }

    private ITblModule validate(@NonNull JsonObject body) {
        ITblModule module = createTblModule(body);
        if (Strings.isBlank(module.getServiceName())) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Service name is mandatory");
        }
        if (Strings.isBlank(module.getVersion())) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Service version is mandatory");
        }
        return module;
    }

    private ITblModule createTblModule(JsonObject body) {
        String serviceId = body.getString("service_id");
        body.remove("service_id");
        RequestedServiceData serviceData = body.isEmpty()
                                           ? new RequestedServiceData()
                                           : JsonData.from(body, RequestedServiceData.class);
        if (Strings.isNotBlank(serviceId)) {
            serviceData.getMetadata().put(SERVICE_ID_KEY, serviceId);
        }
        return verticle.getModuleRule().parse(getDataDir(), serviceData.getMetadata(), serviceData.getAppConfig());
    }

    private Path getDataDir() {
        final String dataDir = verticle.getEntityHandler().sharedData(SharedDataDelegate.SHARED_DATADIR);
        return Strings.isBlank(dataDir) ? FileUtils.DEFAULT_DATADIR : Paths.get(dataDir);
    }

    @Override
    public Map<EventAction, HttpMethod> map() {
        return defaultCRUDMap();
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.CREATE, EventAction.PATCH,
                             EventAction.UPDATE, EventAction.REMOVE);
    }

}
