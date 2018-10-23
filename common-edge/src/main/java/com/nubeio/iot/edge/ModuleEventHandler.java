package com.nubeio.iot.edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nubeio.iot.edge.loader.ModuleType;
import com.nubeio.iot.edge.loader.ModuleTypeFactory;
import com.nubeio.iot.edge.model.gen.Tables;
import com.nubeio.iot.edge.model.gen.tables.pojos.TblModule;
import com.nubeio.iot.share.dto.RequestData;
import com.nubeio.iot.share.event.EventContractor;
import com.nubeio.iot.share.event.EventHandler;
import com.nubeio.iot.share.event.EventModel;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.exceptions.NotFoundException;
import com.nubeio.iot.share.exceptions.NubeException;
import com.nubeio.iot.share.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
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
        return this.verticle.getEntityHandler()
                            .getExecutorSupplier()
                            .get()
                            .execute(context -> context.fetch(Tables.TBL_MODULE))
                            .flattenAsObservable(records -> records)
                            .flatMapSingle(record -> Single.just(record.toJson()))
                            .collect(JsonArray::new, JsonArray::add)
                            .map(results -> new JsonObject().put("services", results));
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
        return this.verticle.handleModule(new TblModule().setServiceId(serviceId), EventType.HALT);
    }

    @EventContractor(values = EventType.UPDATE)
    private Single<JsonObject> update(RequestData data) {
        JsonObject body = data.getBody();
        String serviceId = body.getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            ModuleType moduleType = ModuleTypeFactory.factory(body.getString(Tables.TBL_MODULE.SERVICE_TYPE.getName()));
            JsonObject moduleJson = moduleType.serialize(body);
            String serviceName = moduleJson.getString(Tables.TBL_MODULE.SERVICE_NAME.getName());
            if (Strings.isBlank(serviceName)) {
                throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT,
                                        "Provide at least service_id or service_name");
            }
            return this.verticle.handleModule((TblModule) new TblModule().fromJson(moduleJson), EventType.UPDATE);
        }
        return this.verticle.handleModule((TblModule) new TblModule().fromJson(body), EventType.UPDATE);
    }

    @EventContractor(values = EventType.REMOVE)
    private Single<JsonObject> remove(RequestData data) {
        String serviceId = data.getBody().getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.handleModule(new TblModule().setServiceId(serviceId), EventType.REMOVE);
    }

    @EventContractor(values = EventType.CREATE)
    private Single<JsonObject> create(RequestData data) {
        JsonObject body = data.getBody();
        ModuleType moduleType = ModuleTypeFactory.factory(body.getString(Tables.TBL_MODULE.SERVICE_TYPE.getName()));
        JsonObject moduleJson = moduleType.serialize(body);
        String serviceName = moduleJson.getString(Tables.TBL_MODULE.SERVICE_NAME.getName());
        if (Strings.isBlank(serviceName)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Missing service_name");
        }
        return this.verticle.handleModule((TblModule) new TblModule().fromJson(moduleJson), EventType.CREATE);
    }

}
