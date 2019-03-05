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
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.DeviceEventHandler;

/*
 * Main BACnet verticle
 */
public class BACnetVerticle extends ContainerVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    BACnet bacnetInstance;

    public void start() {
        //        super.start();
        //        logger.info("BACNet configuration: {}", this.nubeConfig.getAppConfig().toJson());
        //        registerEventbus(new EventController(vertx));
    }

    @Override
    public void start(Future<Void> future) {
        super.start();
        logger.info("BACNet configuration: {}", this.nubeConfig.getAppConfig().toJson());
        registerEventbus(new EventController(vertx));
        try {
            String deviceName = this.nubeConfig.getAppConfig().toJson().getString("deviceName");
            int deviceID = this.nubeConfig.getAppConfig().toJson().getInteger("deviceID");
            bacnetInstance = new BACnet(deviceName, deviceID, future, eventController);
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
        vertx.setTimer(1000, id -> {
            JsonObject data = new JsonObject();
            data.put("endpoint", "points");
            data.put("handlerAddress", "nubeiot.edge.connector.bacnet.device.points");
            data.put("driver", "bacnet");
            data.put("action", "GET_LIST");
            EventMessage message = EventMessage.initial(EventAction.CREATE, data);
            eventController.request("nubeiot.edge.connector.driverapi.endpoints", EventPattern.REQUEST_RESPONSE,
                                    message, response -> {
                    //TODO: handle responses
                    logger.info(response.result().body());
                    //                                      if(reply.isSuccess())
                    //                                          System.out.println("Added points/GET_LIST");
                    //                                      else System.out.println("Failed points/GET_LIST");
                });
        });
    }

    private void getPoints() {
        EventMessage message = EventMessage.initial(EventAction.GET_LIST);

        ReplyEventHandler handler = new ReplyEventHandler("BACnet-pointsAPI", EventAction.GET_LIST,
                                                          "nubeiot.edge.connector.bonescript.points", eventMessage -> {
            JsonObject points = eventMessage.getData();
            bacnetInstance.initialiseLocalObjectsFromJson(points);
        }, error -> {
            logger.error(error);
        });

        eventController.fire("nubeiot.edge.connector.bonescript.points", EventPattern.REQUEST_RESPONSE, message,
                             response -> {
                                 handler.accept(response);
                             });
    }

}
