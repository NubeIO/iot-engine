package com.nubeiot.edge.connector.bacnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.handlers.MultipleNetworkEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.NubeServiceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemoteDeviceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemotePointsEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemotePointsInfoEventHandler;
import com.nubeiot.edge.connector.bacnet.service.BACnetDiscoveryService;

/*
 * Main BACnetInstance verticle
 */
public class BACnetVerticle extends ContainerVerticle {

    public static final String DEVICE_METADATA = "BACNET_LOCAL_DEVICE_METADATA";
    protected final Map<String, BACnetInstance> bacnetInstances = new HashMap<>();
    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        final BACnetConfig bacnetConfig = IConfig.from(this.nubeConfig.getAppConfig(), BACnetConfig.class);
        logger.info("BACnet configuration: {}", bacnetConfig.toJson());
        final LocalDeviceMetadata metadata = LocalDeviceMetadata.builder()
                                                                .vendorId(BACnetConfig.VENDOR_ID)
                                                                .vendorName(BACnetConfig.VENDOR_NAME)
                                                                .deviceNumber(bacnetConfig.getDeviceId())
                                                                .modelName(bacnetConfig.getModelName())
                                                                .objectName(bacnetConfig.getDeviceName())
                                                                .slave(bacnetConfig.isAllowSlave())
                                                                .addDiscoverTimeout(bacnetConfig.getDiscoveryTimeout(),
                                                                                    TimeUnit.MILLISECONDS)
                                                                .build();
        this.addSharedData(DEVICE_METADATA, metadata)
            .addProvider(new MicroserviceProvider(), ctx -> microContext = (MicroContext) ctx)
            .registerSuccessHandler(event -> successHandler(bacnetConfig));
    }

    @Override
    public void stop(Future<Void> future) {
        bacnetInstances.forEach((s, bacnet) -> {
            logger.info("Terminating Network Transport {}", s);
            bacnet.terminate();
        });
        super.stop(future);
    }

    private void successHandler(BACnetConfig bacnetConfig) {
        initBACnetService();
        createBACnet(bacnetConfig);
    }

    private void initBACnetService() {
        final EventController client = getEventController();
        Observable.fromIterable(BACnetDiscoveryService.createServices(vertx.getDelegate(), getSharedKey()))
                  .doOnEach(s -> Optional.ofNullable(s.getValue())
                                         .ifPresent(service -> client.register(service.address(), service)))
                  .filter(s -> Objects.nonNull(s.definitions()))
                  .flatMap(s -> registerEndpoint(microContext.getLocalController(), s))
                  .subscribe();
    }

    private void createBACnet(BACnetConfig bacnetConfig) {
        EventController eventClient = getEventController();
        ServiceDiscoveryController localController = microContext.getLocalController();
        startBACnet(bacnetConfig);
        if (bacnetConfig.isAllowSlave()) {
            eventClient.register(BACnetEventModels.NUBE_SERVICE, new NubeServiceEventHandler(bacnetInstances));
        }
        eventClient.register(BACnetEventModels.NETWORKS_ALL, new MultipleNetworkEventHandler(bacnetInstances));
        eventClient.register(BACnetEventModels.DEVICES, new RemoteDeviceEventHandler(bacnetInstances));
        eventClient.register(BACnetEventModels.POINTS_INFO, new RemotePointsInfoEventHandler(bacnetInstances));
        eventClient.register(BACnetEventModels.POINTS, new RemotePointsEventHandler(bacnetInstances));
        publishServices(localController);
        if (bacnetConfig.isAllowSlave()) {
            initLocalPoints(bacnetConfig.getLocalPointsApiAddress(), localController);
        }
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, BACnetDiscoveryService s) {
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

    private void startBACnet(BACnetConfig bacnetConfig) {
        Observable.fromIterable(bacnetConfig.getNetworks().toNetworks())
                  .map(network -> Functions.getIfThrow(() -> TransportProvider.byConfig(network)))
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .flatMapSingle(nw -> BACnetInstance.create(vertx.getDelegate(), getSharedKey(), nw, bacnetInstances))
                  .subscribe();
    }

    private void publishServices(ServiceDiscoveryController localController) {
        localController.addEventMessageRecord("bacnet-local-service", BACnetEventModels.NUBE_SERVICE.getAddress(),
                                              EventMethodDefinition.create("/bacnet/local", () -> {
                                                  Map<EventAction, HttpMethod> map = new HashMap<>();
                                                  map.put(EventAction.CREATE, HttpMethod.POST);
                                                  map.put(EventAction.UPDATE, HttpMethod.PUT);
                                                  map.put(EventAction.PATCH, HttpMethod.PATCH);
                                                  map.put(EventAction.REMOVE, HttpMethod.DELETE);
                                                  return map;
                                              }, false)).subscribe();

        localController.addEventMessageRecord("bacnet-all-network-service", BACnetEventModels.NETWORKS_ALL.getAddress(),
                                              EventMethodDefinition.create("/bacnet/remote", () -> {
                                                  Map<EventAction, HttpMethod> map = new HashMap<>();
                                                  map.put(EventAction.GET_LIST, HttpMethod.GET);
                                                  map.put(EventAction.UPDATE, HttpMethod.PUT);
                                                  return map;
                                              })).subscribe();

        localController.addEventMessageRecord("bacnet-device-service", BACnetEventModels.DEVICES.getAddress(),
                                              EventMethodDefinition.createDefault("/bacnet/remote/:network/device",
                                                                                  "/:deviceId", false)).subscribe();

        localController.addEventMessageRecord("bacnet-points-info-service", BACnetEventModels.POINTS_INFO.getAddress(),
                                              EventMethodDefinition.createDefault(
                                                  "/bacnet/remote/:network/device/:deviceId/points-info", "/:objectId"))
                       .subscribe();

        localController.addEventMessageRecord("bacnet-point-service", BACnetEventModels.POINTS.getAddress(),
                                              EventMethodDefinition.createDefault(
                                                  "/bacnet/remote/:network/device/:deviceId/points", "/:objectId"))
                       .subscribe();
    }

    protected void initLocalPoints(String localPointsAddress, ServiceDiscoveryController localController) {
        logger.info("Requesting local points from address {}", localPointsAddress);
        //        localController.executeHttpService(r -> r.getName().equals("edge-api"), "/edge-api/points",
        //        HttpMethod.GET,
        //                                           RequestData.builder().build())
        //                       .subscribe(responseData -> initLocalPoints(responseData.body()),
        //                                  error -> logger.error(error.getMessage()));
    }

    private void initLocalPoints(JsonObject points) {
        if (points.isEmpty()) {
            return;
        }
        bacnetInstances.values().forEach(instance -> instance.initialiseLocalObjectsFromJson(points));
    }

}
