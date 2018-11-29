package com.nubeiot.edge.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeFactory;
import com.nubeiot.edge.core.model.gen.Tables;
import com.nubeiot.edge.core.model.gen.tables.pojos.TblModule;
import com.nubeiot.edge.core.search.LocalServiceSearch;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;

public final class ModuleEventHandler extends EventHandler {

    private final EdgeVerticle verticle;
    @Getter
    private final List<EventType> availableEvents;

    public ModuleEventHandler(@NonNull EdgeVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(values = EventType.GET_LIST)
    private Single<JsonObject> getList(RequestData data) {
        JsonObject filter = data.getFilter();
        if (filter.getBoolean("available", Boolean.FALSE)) {
            return Single.just(new JsonObject());
        }
        return new LocalServiceSearch(this.verticle.getEntityHandler()).search(data);
    }

    @EventContractor(values = EventType.GET_ONE)
    private Single<JsonObject> getOne(RequestData data) {
        String serviceId = data.getBody().getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .findModuleById(serviceId)
                            .map(o -> o.orElseThrow(() -> new NotFoundException(
                                    String.format("Not found service id '%s'", serviceId))));
    }

    @EventContractor(values = EventType.HALT)
    private Single<JsonObject> halt(RequestData data) {
        String serviceId = data.getBody().getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.processDeploymentTransaction(new TblModule().setServiceId(serviceId), EventType.HALT);
    }

    @EventContractor(values = EventType.UPDATE)
    private Single<JsonObject> update(RequestData data) {
        JsonObject body = data.getBody();
        String serviceId = body.getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            ModuleType moduleType = ModuleTypeFactory.factory(body.getString(Tables.TBL_MODULE.SERVICE_TYPE.getName()));
            JsonObject moduleJson = moduleType.serialize(body, verticle.getModuleRule());
            String serviceName = moduleJson.getString(Tables.TBL_MODULE.SERVICE_NAME.getName());
            if (Strings.isBlank(serviceName)) {
                throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT,
                                        "Provide at least service_id or service_name");
            }
            return this.verticle.processDeploymentTransaction((TblModule) new TblModule().fromJson(moduleJson),
                                                              EventType.UPDATE);
        }
        return this.verticle.processDeploymentTransaction((TblModule) new TblModule().fromJson(body), EventType.UPDATE);
    }

    @EventContractor(values = EventType.REMOVE)
    private Single<JsonObject> remove(RequestData data) {
        String serviceId = data.getBody().getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.processDeploymentTransaction(new TblModule().setServiceId(serviceId), EventType.REMOVE);
    }

    @EventContractor(values = EventType.CREATE)
    private Single<JsonObject> create(RequestData data) {
        JsonObject body = data.getBody();
        ModuleType moduleType = ModuleTypeFactory.factory(body.getString(Tables.TBL_MODULE.SERVICE_TYPE.getName()));
        JsonObject moduleJson = moduleType.serialize(body, this.verticle.getModuleRule());
        String serviceName = moduleJson.getString(Tables.TBL_MODULE.SERVICE_NAME.getName());
        if (Strings.isBlank(serviceName)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Missing service_name");
        }
        return this.verticle.processDeploymentTransaction((TblModule) new TblModule().fromJson(moduleJson),
                                                          EventType.CREATE);
    }

}
