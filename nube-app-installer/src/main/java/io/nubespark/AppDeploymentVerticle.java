package io.nubespark;

import com.nubeio.iot.edge.EdgeVerticle;
import com.nubeio.iot.edge.loader.ModuleType;
import com.nubeio.iot.edge.loader.ModuleTypeFactory;
import com.nubeio.iot.edge.model.gen.Tables;
import com.nubeio.iot.edge.model.gen.tables.pojos.TblModule;
import com.nubeio.iot.share.event.EventMessage;
import com.nubeio.iot.share.event.EventModel;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.exceptions.StateException;

import io.reactivex.Single;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public final class AppDeploymentVerticle extends EdgeVerticle {

    @Override
    protected String getDBName() {
        return "nube-app-installer";
    }

    @Override
    protected Single<JsonObject> initData() {
        logger.info("Setup NubeIO App Installer with config {}", config().encode());
        getVertx().eventBus().consumer(EventModel.MODULE_INSTALLER.getAddress(), this::installer);
        return this.startupModules();
    }

    private void installer(Message<Object> message) {
        EventMessage msg = EventMessage.from(message.body());
        logger.info("Executing action: {} with data: {}", msg.getAction(), msg.toJson().encode());
        JsonObject data = msg.getData();
        handleAction(msg.getAction(), data).subscribe(message::reply, throwable -> {
            logger.error("Cannot deploy new service", throwable);
            message.reply(EventMessage.error(msg.getAction(), throwable).toJson());
        });
    }

    private Single<JsonObject> handleAction(EventType action, JsonObject data) {
        ModuleType moduleType = ModuleTypeFactory.factory(data.getString(Tables.TBL_MODULE.SERVICE_TYPE.getName()));
        if (EventType.CREATE == action) {
            return install(data, moduleType);
        }
        if (EventType.REMOVE == action) {
            return remove(data, moduleType);
        }
        if (EventType.UPDATE == action) {
            return update(data, moduleType);
        }
        throw new StateException("Un-support action " + action);
    }

    private Single<JsonObject> update(JsonObject data, ModuleType moduleType) {
        //TODO not yet implement
        return null;
    }

    private Single<JsonObject> remove(JsonObject data, ModuleType moduleType) {
        String serviceName = data.getString(Tables.TBL_MODULE.SERVICE_NAME.getName());
        return this.entityHandler.findModuleByNameAndType(serviceName, moduleType)
                                 .flatMap(o -> uninstallModule(o.orElseThrow(() -> new StateException(
                                         "Service " + serviceName + " with type " + moduleType +
                                         " is not yet installed"))));
    }

    private Single<JsonObject> install(JsonObject data, ModuleType moduleType) {
        JsonObject moduleJson = moduleType.serialize(data);
        this.entityHandler.findModuleById(moduleJson.getString(Tables.TBL_MODULE.SERVICE_ID.getName()))
                          .doOnSuccess(o -> o.ifPresent(m -> {
                              throw new StateException(
                                      "Service " + m.getServiceId() + " is installed with state " + m.getState());
                          }));
        return installModule((TblModule) new TblModule().fromJson(moduleJson));
    }

}
