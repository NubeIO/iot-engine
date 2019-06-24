package com.nubeiot.edge.connector.bacnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.component.EventControllerBridge;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.edge.connector.bacnet.handlers.DeviceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.NubeServiceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.PointsEventHandler;

/*
 * Main BACnetInstance verticle
 */
public class BACnetVerticle extends ContainerVerticle {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Map<String, BACnetInstance> bacnetInstances = new HashMap<>();

    @Override
    public void start() {
        super.start();

        JsonObject configJson = this.nubeConfig.getAppConfig().toJson();
        BACnetConfig bacnetConfig = IConfig.from(configJson, BACnetConfig.class);
        logger.info("BACNet configuration: {}", configJson);

        if (bacnetConfig.getIpConfigs().isEmpty() && bacnetConfig.getMstpConfigs().isEmpty()) {
            throw new NubeException("No network information provided");
        }

        startBACnet(bacnetConfig);
        initLocalPoints(bacnetConfig.getLocalPointsAddress());
        //TODO: init all configs from DB when ready to implement
        //REGISTER ENDPOINTS
        registerEventbus(EventControllerBridge.getInstance().getEventController(vertx));
        addProvider(new MicroserviceProvider(), this::publishServices);
    }

    @Override
    public void registerEventbus(EventController controller) {
        if (bacnetInstances.isEmpty()) {
            return; //Prevents super.start() from registering before BACnetInstance is started
        }
        controller.register(BACnetEventModels.NUBE_SERVICE, new NubeServiceEventHandler(bacnetInstances));
        controller.register(BACnetEventModels.DEVICES, new DeviceEventHandler(bacnetInstances));
        controller.register(BACnetEventModels.POINTS, new PointsEventHandler(bacnetInstances));
        this.eventController = controller;
    }

    @Override
    public void stop() {
        bacnetInstances.forEach((s, baCnet) -> {
            logger.info("Terminating Network Transport {}", s);
            baCnet.terminate();
        });
    }

    protected void startBACnet(BACnetConfig bacnetConfig) {
        bacnetConfig.getIpConfigs().forEach(ipConfig -> {
            logger.info("Initialising bacnet instance for network {}", ipConfig.getName());
            bacnetInstances.put(ipConfig.getName(),
                                BACnetInstance.createBACnet(bacnetConfig, ipConfig, eventController, vertx));
        });
    }

    protected void publishServices(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("bacnet-local-service", BACnetEventModels.NUBE_SERVICE.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet", "/bacnet"), new JsonObject())
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("bacnet-device-service", BACnetEventModels.DEVICES.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet/:network/",
                                                                               "/bacnet/:network/:deviceID"),
                                           new JsonObject())
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("bacnet-points-service", BACnetEventModels.POINTS.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet/:network/:deviceID/points",
                                                                               "/bacnet/:network/:deviceID/points" +
                                                                               "/:objectID"), new JsonObject())
                    .subscribe();
    }

    protected void initLocalPoints(String localPointsAddress) {
        logger.info("Requesting local points from address {}", localPointsAddress);
        ReplyEventHandler handler = ReplyEventHandler.builder()
                                                     .system("BACNET_API")
                                                     .action(EventAction.GET_LIST)
                                                     .success(this::initLocalPoints)
                                                     .error(error -> logger.error(error.toJson()))
                                                     .build();
        eventController.fire(localPointsAddress, EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_LIST), handler, null);
    }

    private void initLocalPoints(EventMessage msg) {
        final JsonObject data = Objects.isNull(msg.getData()) ? new JsonObject() : msg.getData();
        bacnetInstances.values().forEach(instance -> instance.initialiseLocalObjectsFromJson(data));
    }

}
