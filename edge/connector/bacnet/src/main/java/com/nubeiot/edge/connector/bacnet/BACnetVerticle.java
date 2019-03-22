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
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.edge.connector.bacnet.handlers.DeviceEventHandler;

/*
 * Main BACnet verticle
 */
public class BACnetVerticle extends ContainerVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BACnet bacnetInstance;

    @Override
    public void start(Future<Void> future) {
        super.start();
        logger.info("BACNet configuration: {}", this.nubeConfig.getAppConfig().toJson());

        //START BACNET
        try {
            String deviceName = this.nubeConfig.getAppConfig().toJson().getString("deviceName");
            int deviceID = this.nubeConfig.getAppConfig().toJson().getInteger("deviceID");
            bacnetInstance = new BACnet(deviceName, deviceID, future, eventController, vertx);
        } catch (Exception ex) {
            logger.error("\n\nSTARTUP FAILURE\n\n");
            future.fail(ex);
        }

        //REGISTER ENDPOINTS
        registerEventbus(new EventController(vertx));
        addProvider(new MicroserviceProvider(), this::publishServices);

        future.complete();
        //        vertx.setTimer(2000, handler -> bacnetInstance.BEGIN_TEST());

    }

    public String configFile() { return "bacnet.json"; }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(BACnetEventModels.DEVICES,
                            new DeviceEventHandler(vertx, bacnetInstance, BACnetEventModels.DEVICES));

        controller.register(BACnetEventModels.POINTS,
                            new DeviceEventHandler(vertx, bacnetInstance, BACnetEventModels.POINTS));

        this.eventController = controller;
    }

    private void publishServices(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("event-message-service", BACnetEventModels.DEVICES.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet/devices",
                                                                               "/bacnet/devices/:id"), new JsonObject())
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("event-message-service", BACnetEventModels.POINTS.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet/devices/:id/points",
                                                                               "/bacnet/devices/:id/points/:id"),
                                           new JsonObject())
                    .subscribe();
    }

    private void getLocalPoints() {
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
