package com.nubeiot.edge.connector.bacnet.service.coordinator;

import java.util.Random;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.ErrorMessage;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.event.EventContractor.Param;
import io.github.zero88.qwe.event.Waybill;
import io.github.zero88.qwe.exceptions.CarlException;
import io.github.zero88.qwe.iot.connector.RpcProtocolClient;
import io.github.zero88.qwe.iot.connector.coordinator.Subscriber;
import io.github.zero88.qwe.iot.connector.coordinator.WatcherOption;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.github.zero88.qwe.scheduler.model.job.EventbusJobModel;
import io.github.zero88.qwe.scheduler.model.job.QWEJobModel;
import io.github.zero88.qwe.scheduler.model.trigger.QWETriggerModel;
import io.github.zero88.qwe.scheduler.service.SchedulerRegisterArgs;
import io.reactivex.Observable;
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
import com.nubeiot.edge.connector.bacnet.service.InboundBACnetCoordinator;
import com.nubeiot.edge.connector.bacnet.service.discovery.BACnetObjectExplorer;

import lombok.NonNull;

public final class BACnetCovCoordinator extends AbstractBACnetService
    implements InboundBACnetCoordinator, RpcProtocolClient<BACnetPVEntity>, BACnetApis {

    public BACnetCovCoordinator(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
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
        return ActionMethodMapping.by(ActionMethodMapping.CRUD_MAP, getAvailableEvents());
    }

    @Override
    @EventContractor(action = "CREATE", returnType = Single.class)
    public Single<JsonObject> register(@NonNull RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final WatcherOption option = parseWatcher(requestData);
        final BACnetDevice device = getLocalDeviceFromCache(args);
        return Single.just(option)
                     .filter(WatcherOption::isRealtime)
                     .flatMapSingleElement(opt -> addRealtimeWatcher(device, args, opt))
                     .switchIfEmpty(addPollingWatcher(device, args, option));
    }

    @Override
    @EventContractor(action = "REMOVE", returnType = Single.class)
    public Single<JsonObject> unregister(@NonNull RequestData requestData) {
        return Single.just(new JsonObject());
    }

    @Override
    @EventContractor(action = "MONITOR", returnType = Single.class)
    public boolean monitorThenNotify(@Param("data") JsonObject data, @Param("error") ErrorMessage error) {
        return false;
    }

    @Override
    public @NonNull Observable<Subscriber> subscribers() {
        return null;
    }

    @Override
    public Waybill monitorAddress(JsonObject payload) {
        return Waybill.builder()
                      .address(BACnetObjectExplorer.class.getName())
                      .action(EventAction.GET_ONE)
                      .payload(payload)
                      .build();
    }

    private @NonNull Single<JsonObject> addRealtimeWatcher(BACnetDevice device, DiscoveryArguments args,
                                                           WatcherOption option) {
        final SubscribeCOVOptions covOptions = SubscribeCOVOptions.builder()
                                                                  .subscribe(true)
                                                                  .processId(new Random().nextInt())
                                                                  .lifetime(option.getLifetimeInSeconds())
                                                                  .build();
        final RequestData req = RequestData.builder()
                                           .filter(covOptions.toJson())
                                           .body(listenAddress().toJson())
                                           .build();
        return device.send(EventAction.CREATE, args, req, new SubscribeCOVRequestFactory()).flatMap(msg -> {
            if (msg.isError()) {
                if (option.isFallbackPolling()) {
                    return addPollingWatcher(device, args, option);
                }
                return Single.error(new CarlException(msg.getError().getCode(), msg.getError().getMessage()));
            }
            return Single.just(msg.toJson());
        });
    }

    private Single<JsonObject> addPollingWatcher(BACnetDevice device, DiscoveryArguments args, WatcherOption option) {
        return device.discoverRemoteObject(args).flatMap(pvm -> {
            final RequestData req = RequestData.builder()
                                               .body(args.params().toJson())
                                               .filter(args.options().toJson())
                                               .build();
            final QWEJobModel job = EventbusJobModel.builder()
                                                    .group(protocol().type())
                                                    .name(args.params().buildKey(level()))
                                                    .process(monitorAddress(req.toJson()))
                                                    .callback(listenAddress())
                                                    .build();
            final QWETriggerModel trigger = QWETriggerModel.from(protocol().type(), args.params().buildKey(level()),
                                                                 option.getTriggerOption());
            final SchedulerRegisterArgs schArgs = SchedulerRegisterArgs.builder().job(job).trigger(trigger).build();
            return execute(EventAction.CREATE, RequestData.builder().body(schArgs.toJson()).build().toJson());
        });
    }

}
