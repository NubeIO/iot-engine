package com.nubeiot.edge.connector.bacnet.service.coordinator;

import java.util.Objects;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.ErrorMessage;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.event.EventContractor.Param;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.event.EventbusClient;
import io.github.zero88.qwe.event.Waybill;
import io.github.zero88.qwe.exceptions.CarlException;
import io.github.zero88.qwe.iot.connector.RpcProtocolClient;
import io.github.zero88.qwe.iot.connector.coordinator.CoordinatorInput;
import io.github.zero88.qwe.iot.connector.coordinator.CoordinatorInput.Fields;
import io.github.zero88.qwe.iot.connector.coordinator.CoordinatorRegisterResult;
import io.github.zero88.qwe.iot.connector.coordinator.InboundCoordinator;
import io.github.zero88.qwe.iot.connector.subscriber.Subscriber;
import io.github.zero88.qwe.iot.connector.watcher.WatcherOption;
import io.github.zero88.qwe.iot.connector.watcher.WatcherType;
import io.github.zero88.qwe.micro.ServiceNotFoundException;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.entity.BACnetPVEntity;
import com.nubeiot.edge.connector.bacnet.internal.request.SubscribeCOVRequestFactory;
import com.nubeiot.edge.connector.bacnet.internal.request.SubscribeCOVRequestFactory.SubscribeCOVOptions;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.edge.connector.bacnet.service.discovery.BACnetObjectExplorer;
import com.nubeiot.edge.connector.bacnet.websocket.WebSocketCOVSubscriber;

import lombok.NonNull;

public final class BACnetCOVCoordinator extends AbstractBACnetService
    implements InboundCoordinator<DiscoveryArguments>, RpcProtocolClient<BACnetPVEntity>, BACnetApis {

    public BACnetCOVCoordinator(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public String function() {
        return "cov";
    }

    @Override
    public @NonNull Class<BACnetPVEntity> context() {
        return BACnetPVEntity.class;
    }

    @Override
    public @NonNull String gatewayAddress() {
        return sharedData().getData(BACnetCacheInitializer.GATEWAY_ADDRESS);
    }

    @Override
    public @NonNull String destination() {
        return sharedData().getData(BACnetCacheInitializer.SCHEDULER_SERVICE_NAME);
    }

    @Override
    public DiscoveryLevel level() {
        return DiscoveryLevel.OBJECT;
    }

    @Override
    public @NonNull ActionMethodMapping eventMethodMap() {
        return ActionMethodMapping.CRD_MAP;
    }

    @Override
    @EventContractor(action = "CREATE_OR_UPDATE", returnType = Single.class)
    public Single<CoordinatorRegisterResult> register(@NonNull RequestData requestData) {
        return InboundCoordinator.super.register(requestData);
    }

    @Override
    @EventContractor(action = "REMOVE", returnType = Single.class)
    public Single<JsonObject> unregister(@NonNull RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final BACnetDevice device = getLocalDeviceFromCache(args);
        return Single.just(new JsonObject());
    }

    @Override
    public Single<CoordinatorRegisterResult> addRealtimeWatcher(@NonNull CoordinatorInput<DiscoveryArguments> input) {
        final DiscoveryArguments args = input.getSubject();
        final BACnetDevice device = getLocalDeviceFromCache(args);
        final int lifetime = input.getWatcherOption().getLifetimeInSeconds();
        final SubscribeCOVOptions opt = SubscribeCOVOptions.builder()
                                                           .subscribe(true)
                                                           .processId(device.localDevice().getNextProcessId())
                                                           .lifetime(Math.max(lifetime, 0))
                                                           .build();
        return device.send(EventAction.CREATE, args,
                           RequestData.builder().filter(opt.toJson()).body(listenAddress().toJson()).build(),
                           new SubscribeCOVRequestFactory()).flatMap(msg -> fallbackIfFailure(input, msg));
    }

    @Override
    public Single<CoordinatorRegisterResult> addPollingWatcher(@NonNull CoordinatorInput<DiscoveryArguments> input) {
        final DiscoveryArguments args = input.getSubject();
        final WatcherOption option = input.getWatcherOption();
        final BACnetDevice device = getLocalDeviceFromCache(args);
        final String key = args.key();
        return device.discoverRemoteObject(args)
                     .flatMap(pvm -> addPollingWatcher(option, key, key, RequestData.builder()
                                                                                    .body(args.params().toJson())
                                                                                    .filter(args.options().toJson())
                                                                                    .build()
                                                                                    .toJson()))
                     .map(res -> CoordinatorRegisterResult.from(input, WatcherType.POLLING, res.toJson()));
    }

    @Override
    @EventContractor(action = "GET_ONE", returnType = Single.class)
    public Single<JsonObject> get(@NonNull RequestData requestData) {
        return Single.error(new ServiceNotFoundException("Not yet implemented"));
    }

    @Override
    @EventContractor(action = "MONITOR", returnType = boolean.class)
    public boolean monitorThenNotify(@Param("data") JsonObject data, @Param("error") ErrorMessage error) {
        final EventbusClient eb = EventbusClient.create(sharedData());
        final EventMessage msg = Objects.nonNull(error)
                                 ? EventMessage.error(EventAction.MONITOR, error)
                                 : EventMessage.success(EventAction.MONITOR, data);
        eb.publish(WebSocketCOVSubscriber.builder().build().getPublishAddress(), msg);
        return true;
    }

    @Override
    public @NonNull CoordinatorInput<DiscoveryArguments> parseInput(@NonNull RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final JsonObject body = requestData.body();
        final WatcherOption option = WatcherOption.parse(body.getJsonObject(Fields.watcherOption, new JsonObject()));
        final Subscriber subscriber = WebSocketCOVSubscriber.builder().build();
        return CoordinatorInput.<DiscoveryArguments>builder().subject(args)
                                                             .watcherOption(option)
                                                             .subscriber(subscriber)
                                                             .build();
    }

    @Override
    public Waybill monitorAddress(JsonObject payload) {
        return Waybill.builder()
                      .address(BACnetObjectExplorer.class.getName())
                      .action(EventAction.GET_ONE)
                      .payload(payload)
                      .build();
    }

    private Single<CoordinatorRegisterResult> fallbackIfFailure(CoordinatorInput<DiscoveryArguments> input,
                                                                EventMessage msg) {
        if (!msg.isError()) {
            return Single.just(CoordinatorRegisterResult.from(input, WatcherType.REALTIME, msg.toJson()));
        }
        if (input.getWatcherOption().isFallbackPolling()) {
            logger().warn("Fallback to create polling watcher due to unable to create realtime watcher. Error: {}",
                          msg.getError().toJson());
            return addPollingWatcher(input);
        }
        return Single.error(new CarlException(msg.getError().getCode(), msg.getError().getMessage()));
    }

}
