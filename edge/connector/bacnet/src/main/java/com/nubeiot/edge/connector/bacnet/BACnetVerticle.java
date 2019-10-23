package com.nubeiot.edge.connector.bacnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.handlers.MultipleNetworkEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.NubeServiceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemoteDeviceEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemotePointsEventHandler;
import com.nubeiot.edge.connector.bacnet.handlers.RemotePointsInfoEventHandler;
import com.nubeiot.edge.connector.bacnet.listener.WhoIsListener;
import com.nubeiot.edge.connector.bacnet.service.discover.BACnetDiscoveryService;

import lombok.NonNull;

/*
 * Main BACnetInstance verticle
 */
public final class BACnetVerticle extends AbstractBACnetVerticle<BacnetConfig> {

    protected final Map<String, BACnetInstance> bacnetInstances = new HashMap<>();
    private MicroContext microContext;

    @Override
    public void start() {
        super.start();
        this.addProvider(new MicroserviceProvider(), ctx -> microContext = (MicroContext) ctx);
    }

    @Override
    protected @NonNull Class<BacnetConfig> bacnetConfigClass() {
        return BacnetConfig.class;
    }

    @Override
    protected Maybe<JsonObject> registerServices(@NonNull BacnetConfig config) {
        final EventController client = getEventController();
        return Observable.fromIterable(BACnetDiscoveryService.createServices(getVertx(), getSharedKey()))
                         .doOnEach(s -> Optional.ofNullable(s.getValue())
                                                .ifPresent(service -> client.register(service.address(), service)))
                         .filter(s -> Objects.nonNull(s.definitions()))
                         .flatMap(s -> registerEndpoint(microContext.getLocalController(), s))
                         .map(Record::toJson)
                         .count()
                         .map(total -> new JsonObject().put("message", "Registered " + total + " BACnet service(s)"))
                         .toMaybe();
    }

    @Override
    protected Single<List<BACnetNetwork>> findNetworks(@NonNull BacnetConfig config) {
        return Single.just(new ArrayList<>());
    }

    @Override
    protected BACnetDevice handle(BACnetDevice device) {
        return device.addListener(new WhoIsListener());
    }

    @Override
    protected Future<Void> stopBACnet() {
        bacnetInstances.forEach((s, bacnet) -> {
            logger.info("Terminating Network Transport {}", s);
            bacnet.terminate();
        });
        return Future.succeededFuture();
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, BACnetDiscoveryService s) {
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

    private void createBACnet(BacnetConfig bacnetConfig) {
        EventController eventClient = getEventController();
        ServiceDiscoveryController localController = microContext.getLocalController();
        eventClient.register(BACnetEventModels.NETWORKS_ALL, new MultipleNetworkEventHandler(bacnetInstances));
        eventClient.register(BACnetEventModels.DEVICES, new RemoteDeviceEventHandler(bacnetInstances));
        eventClient.register(BACnetEventModels.POINTS_INFO, new RemotePointsInfoEventHandler(bacnetInstances));
        eventClient.register(BACnetEventModels.POINTS, new RemotePointsEventHandler(bacnetInstances));
        publishServices(localController);
        if (bacnetConfig.isAllowSlave()) {
            eventClient.register(BACnetEventModels.NUBE_SERVICE, new NubeServiceEventHandler(bacnetInstances));
            initLocalPoints(bacnetConfig.getGatewayDiscoverAddress(), localController);
        }
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
