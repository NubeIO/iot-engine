package com.nubeiot.edge.connector.bacnet;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.connector.bacnet.Util.Constants;
import com.nubeiot.edge.connector.bacnet.handlers.DeviceEventHandler;

/*
 * Main BACnet verticle
 */
public class BACnetVerticle extends ContainerVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    BACnet bacnetInstance;

    public void start() {
        super.start();
        logger.info("BACNet configuration: {}", this.nubeConfig.getAppConfig().toJson());
        registerEventbus(new EventController(vertx));
    }

    @Override
    public void start(Future<Void> future) {
        try {
            bacnetInstance = new BACnet(Constants.DeviceName,
                                        this.nubeConfig.getAppConfig().toJson().getInteger("deviceID"), future,
                                        eventController);
            sendAPIEndpoints();
            future.complete();
        } catch (Exception ex) {
            future.fail(ex);
        }
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(BACnetEventModels.DEVICES,
                            new DeviceEventHandler(vertx, bacnetInstance, BACnetEventModels.DEVICES));

        controller.register(BACnetEventModels.POINTS,
                            new DeviceEventHandler(vertx, bacnetInstance, BACnetEventModels.POINTS));

        this.eventController = controller;
    }

    private void sendAPIEndpoints() {
        JsonObject data = new JsonObject();
        data.put("endpoint", "points");
        data.put("handlerAddress", "nubeiot.edge.connector.bacnet.device.points");
        data.put("driver", "bacnet");
        data.put("action", "GET_LIST");

        EventMessage message = EventMessage.initial(EventAction.CREATE, data);

        eventController.fire("nubeiot.edge.connector.driverapi.endpoints", EventPattern.REQUEST_RESPONSE, message,
                             handler -> {
                                 System.out.println("\n\n\n\n\n\n\n" + handler + "????\n\n\n\n\n\n\n");
                             });
    }

}
