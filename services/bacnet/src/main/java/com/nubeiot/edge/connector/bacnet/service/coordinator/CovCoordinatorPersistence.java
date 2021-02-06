package com.nubeiot.edge.connector.bacnet.service.coordinator;

import java.util.Optional;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.ErrorMessage;
import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.exceptions.CarlException;
import io.github.zero88.qwe.exceptions.ServiceException;
import io.github.zero88.qwe.iot.connector.coordinator.CoordinatorChannel;
import io.github.zero88.qwe.iot.connector.rpc.persistence.PersistenceClient;
import io.github.zero88.qwe.storage.json.service.JsonInput;
import io.reactivex.Maybe;
import io.reactivex.Single;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetRpcClient;

import lombok.NonNull;

public final class CovCoordinatorPersistence extends AbstractBACnetRpcClient
    implements PersistenceClient, BACnetProtocol {

    private final CovCoordinatorPersistenceConfig config;

    public CovCoordinatorPersistence(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
        this.config = sharedData().getData(BACnetCacheInitializer.COV_PERSISTENCE_CONFIG);
    }

    @Override
    public @NonNull String destination() {
        return config.getServiceName();
    }

    public Single<CoordinatorChannel> createOrUpdate(@NonNull CoordinatorChannel channel) {
        return execute(EventAction.CREATE_OR_UPDATE, JsonInput.builder()
                                                              .file(config.getFile())
                                                              .pointer("/" + channel.key())
                                                              .dataToInsert(channel.persist())
                                                              .build()).map(c -> channel).toSingle(channel);
    }

    public Single<CoordinatorChannel> remove(@NonNull CoordinatorChannel channel) {
        return execute(EventAction.REMOVE, JsonInput.builder()
                                                    .file(config.getFile())
                                                    .keyToRemove(channel.key())
                                                    .skipRemovedKeyInOutput(true)
                                                    .build()).toSingle();
    }

    public Single<CoordinatorChannel> has(@NonNull String channelKey) {
        return execute(EventAction.parse("HAS"),
                       JsonInput.builder().file(config.getFile()).pointer("/" + channelKey).build()).toSingle();
    }

    public Maybe<CoordinatorChannel> query(@NonNull String channelKey) {
        return execute(EventAction.parse("QUERY"),
                       JsonInput.builder().file(config.getFile()).pointer("/" + channelKey).build());
    }

    private Maybe<CoordinatorChannel> execute(@NonNull EventAction action, @NonNull JsonInput ji) {
        final EventMessage msg = EventMessage.initial(action, RequestData.builder().body(ji.toJson()).build());
        return transporter().request(this.config.getServiceName(), msg)
                            .map(r -> {
                                if (r.isError()) {
                                    final ErrorMessage e = r.getError();
                                    throw new ServiceException("Unable to write to file",
                                                               new CarlException(e.getCode(), e.getMessage()));
                                }
                                return r.getData();
                            })
                            .flatMapMaybe(d -> Optional.ofNullable(d.getJsonObject(ji.getOutputKey()))
                                                       .map(Maybe::just)
                                                       .orElse(Maybe.empty()))
                            .map(o -> JsonData.from(o, CoordinatorChannel.class, JsonData.LENIENT_MAPPER));
    }

}
