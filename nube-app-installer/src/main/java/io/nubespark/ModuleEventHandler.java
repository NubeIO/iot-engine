package io.nubespark;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.nubeio.iot.edge.EdgeVerticle;
import com.nubeio.iot.edge.loader.ModuleType;
import com.nubeio.iot.edge.loader.ModuleTypeFactory;
import com.nubeio.iot.edge.model.gen.Tables;
import com.nubeio.iot.edge.model.gen.tables.pojos.TblModule;
import com.nubeio.iot.share.event.EventModel;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.event.IEventHandler;
import com.nubeio.iot.share.event.RequestData;
import com.nubeio.iot.share.exceptions.NotFoundException;
import com.nubeio.iot.share.exceptions.NubeException;
import com.nubeio.iot.share.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class ModuleEventHandler implements IEventHandler {

    private final EdgeVerticle verticle;
    private final Map<EventType, Function<RequestData, Single<JsonObject>>> mapping = new HashMap<>();

    public ModuleEventHandler(EdgeVerticle verticle) {
        this.verticle = verticle;
        EventModel.EDGE_APP_INSTALLER.getEvents()
                                     .forEach(eventType -> mapping.put(eventType,
                                                                       data -> this.factory(eventType, data)));
    }

    public Single<JsonObject> handle(EventType eventType, RequestData data) {
        final Function<RequestData, Single<JsonObject>> func = mapping.get(eventType);
        if (Objects.isNull(func)) {
            throw new UnsupportedOperationException("Unsupported action " + eventType);
        }
        return func.apply(data);
    }

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
                            .map(results -> new JsonObject().put("apps", results));
    }

    private Single<JsonObject> getOne(RequestData data) {
        String serviceId = data.getBody().getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .findModuleById(serviceId)
                            .map(o -> o.orElseThrow(
                                    () -> new NotFoundException(String.format("Not found service id '%s'", serviceId)))
                                       .toJson());
    }

    private Single<JsonObject> halt(RequestData data) {
        String serviceId = data.getBody().getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.handleModule(new TblModule().setServiceId(serviceId), EventType.HALT);
    }

    private Single<JsonObject> update(RequestData data) {
        JsonObject body = data.getBody();
        String serviceId = body.getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        String serviceName = body.getString(Tables.TBL_MODULE.SERVICE_NAME.getName());
        if (Strings.isBlank(serviceId) && Strings.isBlank(serviceName)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT,
                                    "Provide at least service_id or service_name");
        }
        return this.verticle.handleModule((TblModule) new TblModule().fromJson(body), EventType.UPDATE);
    }

    private Single<JsonObject> remove(RequestData data) {
        String serviceId = data.getBody().getString(Tables.TBL_MODULE.SERVICE_ID.getName());
        if (Strings.isBlank(serviceId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Service Id cannot be blank");
        }
        return this.verticle.handleModule(new TblModule().setServiceId(serviceId), EventType.REMOVE);
    }

    private Single<JsonObject> create(RequestData data) {
        JsonObject body = data.getBody();
        ModuleType moduleType = ModuleTypeFactory.factory(body.getString(Tables.TBL_MODULE.SERVICE_TYPE.getName()));
        String serviceName = body.getString(Tables.TBL_MODULE.SERVICE_NAME.getName());
        if (Strings.isBlank(serviceName)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Missing service_name");
        }
        return this.verticle.handleModule((TblModule) new TblModule().fromJson(moduleType.serialize(body)),
                                          EventType.CREATE);
    }

    //TODO To be removed. Use reflection
    private Single<JsonObject> factory(EventType event, RequestData data) {
        if (EventType.CREATE == event) {
            return create(data);
        }
        if (EventType.REMOVE == event) {
            return remove(data);
        }
        if (EventType.UPDATE == event) {
            return update(data);
        }
        if (EventType.HALT == event) {
            return halt(data);
        }
        if (EventType.GET_ONE == event) {
            return getOne(data);
        }
        if (EventType.GET_LIST == event) {
            return getList(data);
        }
        return null;
    }

}
