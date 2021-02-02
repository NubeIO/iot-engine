package com.nubeiot.edge.connector.bacnet.service.coordinator;

import java.util.Collection;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.iot.connector.coordinator.Channel;
import io.github.zero88.qwe.iot.connector.coordinator.WatcherOption;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.entity.BACnetPVEntity;
import com.nubeiot.edge.connector.bacnet.internal.request.SubscribeCOVRequestFactory;
import com.nubeiot.edge.connector.bacnet.internal.request.SubscribeCOVRequestFactory.SubscribeCOVParams;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.edge.connector.bacnet.service.InboundBACnetCoordinator;

import lombok.NonNull;

public final class BACnetCovCoordinator extends AbstractBACnetService
    implements InboundBACnetCoordinator<BACnetPVEntity>, BACnetApis {

    public BACnetCovCoordinator(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public @NonNull Class<BACnetPVEntity> context() {
        return BACnetPVEntity.class;
    }

    @Override
    public @NonNull String destination() {
        return null;
    }

    @Override
    public Channel channel() {
        return null;
    }

    @Override
    public String function() {
        return "watcher";
    }

    @Override
    public DiscoveryLevel level() {
        return DiscoveryLevel.OBJECT;
    }

    @Override
    public @NonNull ActionMethodMapping eventMethodMap() {
        return ActionMethodMapping.CRUD_MAP;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return eventMethodMap().get().keySet();
    }

    @Override
    @EventContractor(action = "CREATE", returnType = Single.class)
    public Single<JsonObject> register(@NonNull RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final WatcherOption option = parseWatcher(requestData);
        final BACnetDevice device = getLocalDeviceFromCache(args);
        Single<EventMessage> msg;
        if (option.isRealtime()) {
            final SubscribeCOVParams covParams = SubscribeCOVParams.builder().subscribe(true).build();
            msg = device.send(EventAction.CREATE, args, RequestData.builder().body(covParams.toJson()).build(),
                              new SubscribeCOVRequestFactory());
        }
        final SubscribeCOVParams covParams = SubscribeCOVParams.builder().subscribe(true).build();
        return device.send(EventAction.CREATE, args, RequestData.builder().body(covParams.toJson()).build(),
                           new SubscribeCOVRequestFactory()).flatMap(em -> {
            if (em.isError()) {
                return Single.just(em.getError().toJson());
            }
            return registerSchedule(option);
        });
    }

    private Single<JsonObject> registerSchedule(WatcherOption option) {
        return execute(EventAction.CREATE, RequestData.builder()
                                                      .body(new JsonObject().put("trigger",
                                                                                 option.getTriggerOption().toJson())
                                                                            .put("job", new JsonObject()))
                                                      .build());
    }

}
