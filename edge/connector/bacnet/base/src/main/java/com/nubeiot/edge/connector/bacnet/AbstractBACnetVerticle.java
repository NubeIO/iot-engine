package com.nubeiot.edge.connector.bacnet;

import java.util.List;
import java.util.Objects;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.utils.ExecutorHelpers;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;

import lombok.NonNull;

public abstract class AbstractBACnetVerticle<C extends AbstractBACnetConfig> extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        C config = IConfig.from(this.nubeConfig.getAppConfig(), bacnetConfigClass());
        if (logger.isDebugEnabled()) {
            logger.debug("BACnet verticle configuration: {}", config.toJson());
        }
        this.addSharedData(BACnetDevice.EDGE_BACNET_METADATA, LocalDeviceMetadata.from(config))
            .registerSuccessHandler(event -> successHandler(config));
    }

    @Override
    public void stop(Future<Void> future) {
        super.stop(future);
        //        super.stop(future.compose(v -> stopBACnet()));
    }

    protected void successHandler(@NonNull C config) {
        ExecutorHelpers.blocking(getVertx(), this::createDiscoverCompletionHandler)
                       .map(handler -> getEventbusClient().register(config.getCompleteDiscoverAddress(), handler))
                       .flatMapMaybe(ignore -> registerServices(config))
                       .defaultIfEmpty(new JsonObject().put("message", "No BACnet services"))
                       .doOnSuccess(logger::info)
                       .map(r -> config)
                       .flatMapSingle(this::availableNetworks)
                       .doOnSuccess(protocols -> logger.info("Found {} BACnet networks", protocols.size()))
                       .flattenAsObservable(protocols -> protocols)
                       .filter(Objects::nonNull)
                       .map(protocol -> new BACnetDevice(getVertx(), getSharedKey(), protocol))
                       .map(this::onEachStartup)
                       .map(BACnetDevice::asyncStart)
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
     * Provide available BACnet networks
     *
     * @param config BACnet config
     * @return single of list networks
     * @see CommunicationProtocol
     */
    protected abstract @NonNull Single<List<CommunicationProtocol>> availableNetworks(@NonNull C config);

    protected abstract BACnetDevice onEachStartup(BACnetDevice device);

    protected abstract Future<Void> stopBACnet();

    protected DiscoverCompletionHandler createDiscoverCompletionHandler() {
        return new DiscoverCompletionHandler();
    }

}
