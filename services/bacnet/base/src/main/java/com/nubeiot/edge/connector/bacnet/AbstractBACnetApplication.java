package com.nubeiot.edge.connector.bacnet;

import java.util.Objects;

import io.github.zero88.qwe.IConfig;
import io.github.zero88.qwe.component.ApplicationVerticle;
import io.github.zero88.qwe.component.ContextLookup;
import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.utils.ExecutorHelpers;
import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.serotonin.bacnet4j.event.DeviceEventListener;

import lombok.NonNull;

public abstract class AbstractBACnetApplication<C extends BACnetConfig> extends ApplicationVerticle {

    @Override
    public void start() {
        super.start();
        C bacnetConfig = IConfig.from(this.config.getAppConfig(), bacnetConfigClass());
        if (logger.isDebugEnabled()) {
            logger.debug("BACnet application configuration: {}", bacnetConfig.toJson());
        }
        this.addData(BACnetDevice.CONFIG_KEY, bacnetConfig);
    }

    @Override
    public void stop(Promise<Void> future) {
        shutdown().doOnSuccess(result -> logger.info(result.encode()))
                  .subscribe(ignore -> super.stop(future), future::fail);
    }

    @Override
    public void onInstallCompleted(@NonNull ContextLookup lookup) {
        C bacnetConfig = sharedData().getData(BACnetDevice.CONFIG_KEY);
        ExecutorHelpers.blocking(getVertx(), this::getEventbus)
                       .map(c -> c.register(bacnetConfig.getCompleteDiscoverAddress(),
                                            createDiscoverCompletionHandler()))
                       .flatMap(c -> registerApis(sharedData(), bacnetConfig).doOnSuccess(o -> logger.info(o.encode())))
                       .flatMap(ignore -> initialize(bacnetConfig).doOnSuccess(o -> logger.info(o.encode())))
                       .subscribe((d, e) -> readinessHandler(bacnetConfig, d, e));
    }

    protected void readinessHandler(@NonNull C config, JsonObject d, Throwable e) {
        final EventMessage msg = Objects.nonNull(e)
                                 ? EventMessage.error(EventAction.NOTIFY_ERROR, e)
                                 : EventMessage.initial(EventAction.NOTIFY, RequestData.builder().body(d).build());
        getEventbus().publish(config.getReadinessAddress(), msg);
    }

    /**
     * Register BACnet config class to parsing data
     *
     * @return BACnet config class
     */
    @NonNull
    protected abstract Class<C> bacnetConfigClass();

    /**
     * Register {@code BACnet API services} to {@code IoT gateway}
     *
     * @param sharedData Shared data proxy
     * @param config     BACnet config
     * @return maybe result or maybe empty
     * @see BACnetApis
     */
    @NonNull
    protected abstract Single<JsonObject> registerApis(@NonNull SharedDataLocalProxy sharedData, @NonNull C config);

    /**
     * Add one or more {@code BACnet listeners} after each {@code BACnet device} on each network starts
     *
     * @param device BACnet device
     * @see BACnetDevice
     * @see DeviceEventListener
     */
    protected abstract void addListenerOnEachDevice(@NonNull BACnetDevice device);

    @NonNull
    protected DiscoverCompletionHandler createDiscoverCompletionHandler() {
        return new DiscoverCompletionHandler();
    }

    protected Single<JsonObject> initialize(@NonNull C config) {
        return Single.just(new JsonObject().put("message", "No initialize service"));
    }

    protected Single<JsonObject> shutdown() {
        return Single.just(new JsonObject());
    }

}
