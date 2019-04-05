package com.nubeiot.edge.connector.bacnet;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.http.HttpClient;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.edge.connector.bacnet.handlers.DeviceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.PointsEventHandler;

/*
 * Main BACnet verticle
 */
public class BACnetVerticle extends ContainerVerticle {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected BACnet bacnetInstance;

    @Override
    public void start(Future<Void> future) {
        super.start();
        logger.info("BACNet configuration: {}", this.nubeConfig.getAppConfig().toJson());
        try {
            startBACnet();
        } catch (Exception e) {
            logger.error(e);
            future.fail(e);
            return;
        }

        //REGISTER ENDPOINTS
        registerEventbus(new EventController(vertx));
        //        addProvider(new MicroserviceProvider(), this::publishServices);
        super.start(future);
    }

    protected void startBACnet() throws Exception {
        String deviceName = this.nubeConfig.getAppConfig().toJson().getString("deviceName");
        int deviceID = this.nubeConfig.getAppConfig().toJson().getInteger("deviceID");
        String networkInterfaceName = this.nubeConfig.getAppConfig().toJson().getString("networkInterface");
        bacnetInstance = BACnet.createBACnet(deviceName, deviceID, eventController, vertx, networkInterfaceName);
        getLocalPoints();
    }

    @Override
    public void stop(Future<Void> future) {
        bacnetInstance.terminate();
        super.stopUnits(future);
    }

    public String configFile() { return "bacnet.json"; }

    @Override
    public void registerEventbus(EventController controller) {
        if (bacnetInstance == null) {
            return; //Prevents super.start() from registering before BACnet is started
        }
        controller.register(BACnetEventModels.DEVICES, new DeviceEventHandler(vertx, bacnetInstance));

        controller.register(BACnetEventModels.POINTS, new PointsEventHandler(vertx, bacnetInstance));

        this.eventController = controller;
    }

    protected void publishServices(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("bacnet-device-service", BACnetEventModels.DEVICES.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet/devices",
                                                                               "/bacnet/devices/:deviceID"),
                                           new JsonObject())
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("bacnet-points-service", BACnetEventModels.POINTS.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet/devices/:deviceID/points",
                                                                               "/bacnet/devices/:deviceID/points" +
                                                                               "/:objectID"), new JsonObject())
                    .subscribe();
    }

    protected void getLocalPoints() {
        //        EventMessage message = EventMessage.initial(EventAction.GET_LIST);
        //
        //        ReplyEventHandler handler = new ReplyEventHandler("BACnet-pointsAPI", EventAction.GET_LIST,
        //                                                          "nubeiot.edge.connector.bonescript.points",
        //                                                          eventMessage -> {
        //            JsonObject points = eventMessage.getData();
        //            bacnetInstance.initialiseLocalObjectsFromJson(points);
        //        }, error -> {
        //            logger.error(error);
        //        });
        //
        //        eventController.fire("nubeiot.edge.connector.bonescript.points", EventPattern.REQUEST_RESPONSE,
        //        message,
        //                             response -> {
        //                                 handler.accept(response);
        //                             });

        HttpClient client = vertx.createHttpClient().getNow(4000, "localhost", "/points", response -> {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                response.bodyHandler(body -> {
                    bacnetInstance.initialiseLocalObjectsFromJson(body.toJsonObject());
                });
            } else {
                System.out.println("REQUEST DIDNT ");
            }
        });
    }

}
