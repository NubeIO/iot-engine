package com.nubeiot.edge.connector.bacnet;

import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.utils.ExecutorHelpers;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.edge.connector.bacnet.service.BACnetNotifier;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.serotonin.bacnet4j.event.DeviceEventListener;

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
    }

    protected void successHandler(@NonNull C config) {
        ExecutorHelpers.blocking(getVertx(), this::getEventbusClient)
                       .map(c -> c.register(config.getCompleteDiscoverAddress(), createDiscoverCompletionHandler()))
                       .flatMap(client -> registerApis(client, config).map(ignore -> client))
                       .flatMap(client -> registerSubscriber(client, config))
                       .map(r -> config)
                       .flatMap(this::availableNetworks)
                       .doOnSuccess(protocols -> logger.info("Found {} BACnet networks", protocols.size()))
                       .flattenAsObservable(protocols -> protocols)
                       .filter(Objects::nonNull)
                       .map(protocol -> new BACnetDevice(getVertx(), getSharedKey(), protocol))
                       .map(this::addListenerOnEachDevice)
                       .map(BACnetDevice::asyncStart)
                       .count()
                       .map(total -> new JsonObject().put("total", total))
                       .subscribe((d, e) -> readinessHandler(config, d, e));
    }

    private void readinessHandler(@NonNull C config, JsonObject d, Throwable e) {
        final RequestData reqData = Objects.isNull(e)
                                    ? RequestData.builder().body(d).build()
                                    : RequestData.builder().body(ErrorMessage.parse(e).toJson()).build();
        getEventbusClient().publish(config.getReadinessAddress(), EventMessage.initial(EventAction.NOTIFY, reqData));
    }

    /**
     * Register BACnet config class to parsing data
     *
     * @return BACnet config class
     */
    @NonNull
    protected abstract Class<C> bacnetConfigClass();

    /**
     * Register {@code BACnet API services} to {@code edge gateway}
     *
     * @param client Eventbus client
     * @param config BACnet config
     * @return maybe result or maybe empty
     * @see BACnetApis
     */
    @NonNull
    protected abstract Single<JsonObject> registerApis(@NonNull EventbusClient client, @NonNull C config);

    /**
     * Register {@code BACnet Subscriber} services
     *
     * @param client Eventbus client
     * @param config BACnet config
     * @return maybe result or maybe empty
     * @see BACnetSubscriber
     */
    @NonNull
    protected abstract Single<JsonObject> registerSubscriber(@NonNull EventbusClient client, @NonNull C config);

    /**
     * Add one or more {@code BACnet listeners} after each {@code BACnet device} on each network starts
     *
     * @param device BACnet device
     * @return BACnet device for fluent API
     * @see DeviceEventListener
     * @see BACnetNotifier
     */
    @NonNull
    protected abstract BACnetDevice addListenerOnEachDevice(@NonNull BACnetDevice device);

    /**
     * Provide available BACnet networks
     *
     * @param config BACnet config
     * @return single of list networks
     * @see CommunicationProtocol
     */
    protected abstract @NonNull Single<List<CommunicationProtocol>> availableNetworks(@NonNull C config);

    protected abstract Future<Void> stopBACnet();

    @NonNull
    protected DiscoverCompletionHandler createDiscoverCompletionHandler() {
        return new DiscoverCompletionHandler();
    }

}
