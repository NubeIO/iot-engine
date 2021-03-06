package com.nubeiot.edge.connector.bacnet;

import java.util.Collection;
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
import com.nubeiot.core.exceptions.ErrorData;
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
        stopBACnet().doOnSuccess(result -> logger.info(result.encode()))
                    .subscribe(ignore -> super.stop(future), future::fail);
    }

    protected void successHandler(@NonNull C config) {
        ExecutorHelpers.blocking(getVertx(), this::getEventbusClient)
                       .map(c -> c.register(config.getCompleteDiscoverAddress(), createDiscoverCompletionHandler()))
                       .flatMap(client -> registerApis(client, config).doOnSuccess(logger::info).map(ignore -> client))
                       .flatMap(client -> invokeSubscriberRegistration(client, config).doOnSuccess(logger::info))
                       .map(r -> config)
                       .flatMap(this::availableNetworks)
                       .doOnSuccess(protocols -> logger.info("Found {} BACnet networks", protocols.size()))
                       .flattenAsObservable(protocols -> protocols)
                       .map(protocol -> BACnetDeviceInitializer.builder()
                                                               .vertx(getVertx())
                                                               .sharedKey(getSharedKey())
                                                               .preFunction(this::addListenerOnEachDevice)
                                                               .build()
                                                               .asyncStart(protocol))
                       .count()
                       .map(total -> new JsonObject().put("total", total))
                       .subscribe((d, e) -> readinessHandler(config, d, e));
    }

    private Single<JsonObject> invokeSubscriberRegistration(EventbusClient client, C config) {
        if (config.isEnableSubscriber()) {
            return registerSubscriber(client, config);
        }
        return Single.just(new JsonObject().put("message", "BACnet subscriber feature is disabled"));
    }

    private void readinessHandler(@NonNull C config, JsonObject d, Throwable e) {
        final EventMessage msg = Objects.nonNull(e)
                                 ? EventMessage.initial(EventAction.NOTIFY_ERROR,
                                                        ErrorData.builder().throwable(e).build())
                                 : EventMessage.initial(EventAction.NOTIFY, RequestData.builder().body(d).build());
        getEventbusClient().publish(config.getReadinessAddress(), msg);
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
     * @see BACnetDevice
     * @see DeviceEventListener
     * @see BACnetNotifier
     */
    protected abstract void addListenerOnEachDevice(@NonNull BACnetDevice device);

    /**
     * Provide available BACnet networks
     *
     * @param config BACnet config
     * @return single of list networks
     * @see CommunicationProtocol
     */
    protected abstract @NonNull Single<? extends Collection<CommunicationProtocol>> availableNetworks(
        @NonNull C config);

    protected abstract Single<JsonObject> stopBACnet();

    @NonNull
    protected DiscoverCompletionHandler createDiscoverCompletionHandler() {
        return new DiscoverCompletionHandler();
    }

}
