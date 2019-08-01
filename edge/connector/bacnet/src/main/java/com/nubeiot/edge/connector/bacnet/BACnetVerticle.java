package com.nubeiot.edge.connector.bacnet;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.connector.bacnet.handlers.MultipleNetworkEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.NubeServiceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemoteDeviceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemotePointEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemotePointsInfoEventHandler;

/*
 * Main BACnetInstance verticle
 */
public class BACnetVerticle extends ContainerVerticle {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Map<String, BACnetInstance> bacnetInstances = new HashMap<>();
    protected MicroContext microContext;
    protected boolean allowSlave;

    @Override
    public void start() {
        super.start();

        JsonObject configJson = this.nubeConfig.getAppConfig().toJson();
        BACnetConfig bacnetConfig = IConfig.from(configJson, BACnetConfig.class);
        logger.info("BACNet configuration: {}", configJson);

        allowSlave = bacnetConfig.isAllowSlave();

        if (bacnetConfig.getIpConfigs().isEmpty() && bacnetConfig.getMstpConfigs().isEmpty()) {
            throw new NubeException("No network information provided");
        }

        addProvider(new MicroserviceProvider(), microContext -> {
            ServiceDiscoveryController localController = ((MicroContext) microContext).getLocalController();
            this.microContext = (MicroContext) microContext;
            startBACnet(bacnetConfig, localController);
            registerEventbus(new EventController(vertx));
            publishServices(localController);
            if (allowSlave) {
                initLocalPoints(bacnetConfig.getLocalPointsApiAddress(), localController);
            }
        });
    }

    @Override
    public void registerEventbus(EventController controller) {
        if (bacnetInstances.isEmpty()) {
            return; //Prevents super.start() from registering before BACnetInstance is started
        }
        if (allowSlave) {
            controller.register(BACnetEventModels.NUBE_SERVICE, new NubeServiceEventHandler(bacnetInstances));
        }
        controller.register(BACnetEventModels.NETWORKS_ALL, new MultipleNetworkEventHandler(bacnetInstances));
        controller.register(BACnetEventModels.DEVICES, new RemoteDeviceEventHandler(bacnetInstances));
        controller.register(BACnetEventModels.POINTS_INFO, new RemotePointsInfoEventHandler(bacnetInstances));
        controller.register(BACnetEventModels.POINT, new RemotePointEventHandler(bacnetInstances));
        this.eventController = controller;
    }

    @Override
    public void stop(Future<Void> future) {
        bacnetInstances.forEach((s, baCnet) -> {
            logger.info("Terminating Network Transport {}", s);
            baCnet.terminate();
        });
        this.stopUnits(future);
    }

    protected void startBACnet(BACnetConfig bacnetConfig, ServiceDiscoveryController localController) {
        bacnetConfig.getIpConfigs().forEach(ipConfig -> {
            logger.info("Initialising bacnet instance for network {}", ipConfig.getName());
            bacnetInstances.put(ipConfig.getName(),
                                BACnetInstance.createBACnet(bacnetConfig, ipConfig, eventController, localController,
                                                            bacnetInstances, vertx));
        });
    }

    protected void publishServices(ServiceDiscoveryController localController) {
        localController.addEventMessageRecord("bacnet-local-service", BACnetEventModels.NUBE_SERVICE.getAddress(),
                                              EventMethodDefinition.create("/bacnet/local", new ActionMethodMapping() {
                                                  @Override
                                                  public Map<EventAction, HttpMethod> get() {
                                                      Map<EventAction, HttpMethod> map = new HashMap<>();
                                                      map.put(EventAction.CREATE, HttpMethod.POST);
                                                      map.put(EventAction.UPDATE, HttpMethod.PUT);
                                                      map.put(EventAction.PATCH, HttpMethod.PATCH);
                                                      map.put(EventAction.REMOVE, HttpMethod.DELETE);
                                                      return map;
                                                  }
                                              }, false)).subscribe();

        localController.addEventMessageRecord("bacnet-all-network-service", BACnetEventModels.NETWORKS_ALL.getAddress(),
                                              EventMethodDefinition.create("/bacnet/remote", new ActionMethodMapping() {
                                                  @Override
                                                  public Map<EventAction, HttpMethod> get() {
                                                      Map<EventAction, HttpMethod> map = new HashMap<>();
                                                      map.put(EventAction.GET_LIST, HttpMethod.GET);
                                                      map.put(EventAction.UPDATE, HttpMethod.PUT);
                                                      return map;
                                                  }
                                              }, true)).subscribe();

        localController.addEventMessageRecord("bacnet-device-service", BACnetEventModels.DEVICES.getAddress(),
                                              EventMethodDefinition.createDefault("/bacnet/remote/:network/device",
                                                                                  "/bacnet/remote/:network/device" +
                                                                                  "/:deviceId", false)).subscribe();

        localController.addEventMessageRecord("bacnet-points-info-service", BACnetEventModels.POINTS_INFO.getAddress(),
                                              EventMethodDefinition.createDefault(
                                                  "/bacnet/remote/:network/device/:deviceId/points-info",
                                                  "/bacnet/remote/:network/device/:deviceId/points-info/:objectId",
                                                  false)).subscribe();

        localController.addEventMessageRecord("bacnet-point-service", BACnetEventModels.POINT.getAddress(),
                                              EventMethodDefinition.createDefault(
                                                  "/bacnet/remote/:network/device/:deviceId/point",
                                                  "/bacnet/remote/:network/device/:deviceId/point/:objectId", true))
                       .subscribe();
    }

    protected void initLocalPoints(String localPointsAddress, ServiceDiscoveryController localController) {
        logger.info("Requesting local points from address {}", localPointsAddress);

        //TODO: need auth for bs-api??
        localController.executeHttpService(r -> r.getName().equals("edge-api"), "/edge-api/points", HttpMethod.GET,
                                           RequestData.builder().build())
                       .subscribe(responseData -> initLocalPoints(responseData.body()),
                                  error -> logger.error(error.getMessage()));
    }

    private void initLocalPoints(JsonObject points) {
        if (points.isEmpty()) {
            return;
        }
        bacnetInstances.values().forEach(instance -> instance.initialiseLocalObjectsFromJson(points));
    }

}
