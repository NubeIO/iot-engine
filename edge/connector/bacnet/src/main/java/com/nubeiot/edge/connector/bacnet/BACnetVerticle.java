package com.nubeiot.edge.connector.bacnet;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.edge.connector.bacnet.handlers.DeviceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.PointsEventHandler;

/*
 * Main BACnetInstance verticle
 */
public class BACnetVerticle extends ContainerVerticle {

    //    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected Map<String, BACnetInstance> bacnetInstances;

    @Override
    public void start() {
        super.start();

        JsonObject configJson = this.nubeConfig.getAppConfig().toJson();
        BACnetConfig bacnetConfig = IConfig.from(configJson, BACnetConfig.class);
        logger.info("BACNet configuration: {}", configJson);

        if (bacnetConfig.getIpConfigs().isEmpty() && bacnetConfig.getMstpConfigs().isEmpty()) {
            throw new NubeException("No network information provided");
        }

        bacnetInstances = new HashMap<>();
        startBACnet(bacnetConfig);
        initLocalPoints(bacnetConfig.getLocalPointsAddress());
        //REGISTER ENDPOINTS
        registerEventbus(new EventController(vertx));
        //        addProvider(new MicroserviceProvider(), this::publishServices);
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

    @Override
    public void registerEventbus(EventController controller) {
        if (bacnetInstances == null) {
            return; //Prevents super.start() from registering before BACnetInstance is started
        }
        controller.register(BACnetEventModels.DEVICES, new DeviceEventHandler(bacnetInstances));
        controller.register(BACnetEventModels.POINTS, new PointsEventHandler(bacnetInstances));
        this.eventController = controller;
    }

    protected void publishServices(MicroContext microContext) {
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

        //TODO: TEMPORARY REQUEST
        vertx.createHttpClient().getNow(4000, "localhost", "/points", response -> {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                response.bodyHandler(body -> {
                    //todo: init points for each instance
                });
            } else {
                System.out.println("REQUEST DIDNT ");
            }
        });
    }

}
