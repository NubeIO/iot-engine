package com.nubeiot.edge.installer.service;

import java.util.Collection;
import java.util.Optional;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.model.dto.PreDeploymentResult;
import com.nubeiot.edge.installer.model.dto.RequestedServiceData;
import com.nubeiot.edge.installer.model.tables.daos.ApplicationDao;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;
import com.nubeiot.edge.installer.model.tables.pojos.Application;
import com.nubeiot.edge.installer.search.LocalServiceSearch;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ApplicationService implements InstallerService {

    @NonNull
    private final InstallerEntityHandler entityHandler;

    public final String servicePath() {
        return "";
    }

    public final String paramPath() {
        return "app_id";
    }

    @Override
    public final @NonNull Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.CRUD_MAP.get().keySet();
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        JsonObject filter = data.filter();
        if (filter.getBoolean("available", Boolean.FALSE)) {
            return Single.just(new JsonObject());
        }
        return new LocalServiceSearch(entityHandler).search(data);
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        String serviceId = data.body().getString(paramPath());
        if (Strings.isBlank(serviceId)) {
            throw new IllegalArgumentException("Service id is mandatory");
        }
        return entityHandler.dao(ApplicationDao.class)
                            .findOneById(serviceId)
                            .map(o -> o.map(this::removeCredentialsInAppConfig))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .switchIfEmpty(
                                Single.error(new NotFoundException("Not found service id '" + serviceId + "'")));
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData data) {
        IApplication module = createTblModule(data.body());
        if (Strings.isBlank(module.getAppId())) {
            throw new IllegalArgumentException("Service id is mandatory");
        }
        return new AppDeploymentWorkflow(entityHandler).process(module, EventAction.PATCH);
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        IApplication module = validate(data.body());
        if (Strings.isBlank(module.getServiceName()) && Strings.isBlank(module.getAppId())) {
            throw new IllegalArgumentException("Provide at least service id or service name");
        }
        return new AppDeploymentWorkflow(entityHandler).process(module, EventAction.UPDATE);
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> remove(RequestData data) {
        IApplication module = new Application().setAppId(data.body().getString(paramPath()));
        if (Strings.isBlank(module.getAppId())) {
            throw new IllegalArgumentException("Service id is mandatory");
        }
        return new AppDeploymentWorkflow(entityHandler).process(module, EventAction.REMOVE);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData data) {
        return new AppDeploymentWorkflow(entityHandler).process(validate(data.body()), EventAction.CREATE);
    }

    private JsonObject removeCredentialsInAppConfig(Application record) {
        record.setAppConfig(PreDeploymentResult.filterOutSensitiveConfig(record.getAppId(), record.getAppConfig()));
        return record.toJson();
    }

    private IApplication validate(@NonNull JsonObject body) {
        IApplication module = createTblModule(body);
        if (Strings.isBlank(module.getServiceName())) {
            throw new IllegalArgumentException("Service name is mandatory");
        }
        if (Strings.isBlank(module.getVersion())) {
            throw new IllegalArgumentException("Service version is mandatory");
        }
        return module;
    }

    private IApplication createTblModule(JsonObject body) {
        String serviceId = body.getString(paramPath());
        body.remove(paramPath());
        RequestedServiceData serviceData = body.isEmpty()
                                           ? new RequestedServiceData()
                                           : JsonData.from(body, RequestedServiceData.class);
        if (Strings.isNotBlank(serviceId)) {
            serviceData.getMetadata().put(paramPath(), serviceId);
        }
        final ModuleTypeRule rule = entityHandler.sharedData(InstallerEntityHandler.SHARED_MODULE_RULE);
        return rule.parse(entityHandler.dataDir(), serviceData.getMetadata(), serviceData.getAppConfig());
    }

}
