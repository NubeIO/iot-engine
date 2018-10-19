package io.nubespark;

import com.nubeio.iot.edge.EdgeVerticle;
import com.nubeio.iot.share.event.EventMessage;
import com.nubeio.iot.share.event.EventModel;
import com.nubeio.iot.share.event.IEventHandler;
import com.nubeio.iot.share.event.RequestData;
import com.nubeio.iot.share.exceptions.NubeException;

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
        logger.info("Setup NubeIO App Installer with config {}", getAppConfig().encode());
        getVertx().eventBus()
                  .consumer(EventModel.EDGE_APP_INSTALLER.getAddress(),
                            message -> this.handleEvent(message, new ModuleEventHandler(this)));
        getVertx().eventBus()
                  .consumer(EventModel.EDGE_APP_TRANSACTION.getAddress(),
                            message -> this.handleEvent(message, new TransactionEventHandler(this)));
        return this.startupModules();
    }

    private void handleEvent(Message<Object> message, IEventHandler eventHandler) {
        EventMessage msg = EventMessage.from(message.body());
        logger.info("Executing action: {} with data: {}", msg.getAction(), msg.toJson().encode());
        try {
            eventHandler.handle(msg.getAction(), msg.getData().mapTo(RequestData.class))
                        .subscribe(data -> message.reply(EventMessage.success(msg.getAction(), data).toJson()),
                                   throwable -> {
                                       logger.error("Failed when handle event", throwable);
                                       message.reply(EventMessage.error(msg.getAction(), throwable).toJson());
                                   });
        } catch (NubeException ex) {
            logger.error("Failed when handle event", ex);
            message.reply(EventMessage.error(msg.getAction(), ex).toJson());
        }
    }

}
