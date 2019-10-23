package com.nubeiot.edge.connector.bacnet;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.utils.ExecutorHelpers;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;

import lombok.NonNull;

public abstract class AbstractBACnetVerticle<C extends AbstractBACnetConfig> extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        C config = IConfig.from(this.nubeConfig.getAppConfig(), bacnetConfigClass());
        logger.debug(config.toJson());
        this.addSharedData(BACnetDevice.EDGE_BACNET_METADATA, LocalDeviceMetadata.from(config))
            .registerSuccessHandler(event -> successHandler(config));
    }

    @Override
    public void stop(Future<Void> future) {
        super.stop(future.compose(v -> stopBACnet()));
    }

    protected void successHandler(@NonNull C config) {
        ExecutorHelpers.blocking(getVertx(), registerServices(config))
                       .defaultIfEmpty(new JsonObject().put("message", "No BACnet services"))
                       .doOnSuccess(logger::info)
                       .map(r -> config)
                       .flatMapSingle(this::findNetworks)
                       .doOnSuccess(networks -> logger.info("Found {} BACnet networks", networks.size()))
                       .flatMapObservable(this::startBACnet)
                       .count()
                       .doOnSuccess(total -> logger.info("Start {} BACnet devices", total))
                       .subscribe();
    }

    /**
     * Register BACnet config class to parsing data
     *
     * @return BACnet config class
     */
    @NonNull
    protected abstract Class<C> bacnetConfigClass();

    /**
     * Register BACnet services both public endpoint to {@code edge gateway} and internal services
     *
     * @param config BACnet config
     * @return maybe result or maybe empty
     */
    @NonNull
    protected abstract Maybe<JsonObject> registerServices(@NonNull C config);

    /**
     * Find BACnet networks
     *
     * @param config BACnet config
     * @return single of list networks
     * @see BACnetNetwork
     */
    @NonNull
    protected abstract Single<List<BACnetNetwork>> findNetworks(@NonNull C config);

    private Observable<BACnetDevice> startBACnet(List<BACnetNetwork> networks) {
        return Observable.fromIterable(networks)
                         .filter(Objects::nonNull)
                         .map(network -> new BACnetDevice(getVertx(), getSharedKey(), network))
                         .map(this::handle)
                         .doOnEach(notification -> Optional.ofNullable(notification.getValue())
                                                           .ifPresent(BACnetDevice::start));
    }

    protected abstract BACnetDevice handle(BACnetDevice device);

    protected abstract Future<Void> stopBACnet();

}
