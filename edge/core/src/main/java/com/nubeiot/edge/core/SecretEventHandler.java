package com.nubeiot.edge.core;

import static com.nubeiot.eventbus.edge.EdgeInstallerEventBus.BIOS_SECRET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.exceptions.NotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecretEventHandler implements EventHandler {

    private final EdgeVerticle verticle;

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        String serviceId = data.body().getString("service_id");
        return this.verticle.getEntityHandler()
                            .findModuleById(serviceId)
                            .map(o -> o.orElseThrow(
                                () -> new NotFoundException(String.format("Not found module_id '%s'", serviceId))))
                            .flatMap(o -> Single.just(JsonData.keepKeys(o.toJson(), "secret_config")));
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        JsonObject body = data.body();
        String serviceId = body.getString("service_id");
        body.remove("service_id");
        RequestedServiceData requestedServiceData = JsonData.from(body, RequestedServiceData.class);
        return this.verticle.getEntityHandler()
                            .findModuleById(serviceId)
                            .map(o -> o.orElseThrow(
                                () -> new NotFoundException(String.format("Not found module_id '%s'", serviceId))))
                            .flatMap(module -> {
                                System.out.println("Secret Config....");
                                System.out.println(
                                    "Secret Config...." + requestedServiceData.getSecretConfig().toJson().encode());
                                module.setSecretConfig(requestedServiceData.getSecretConfig().toJson());
                                return this.verticle.getEntityHandler()
                                                    .processDeploymentTransaction(module, EventAction.UPDATE);
                            });
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> updatePartly(RequestData data) {
        JsonObject body = data.body();
        String serviceId = body.getString("service_id");
        body.remove("service_id");
        RequestedServiceData requestedServiceData = JsonData.from(body, RequestedServiceData.class);
        return this.verticle.getEntityHandler()
                            .findModuleById(serviceId)
                            .map(o -> o.orElseThrow(
                                () -> new NotFoundException(String.format("Not found module_id '%s'", serviceId))))
                            .flatMap(module -> {
                                JsonObject secretConfig = module.getSecretConfig()
                                                                .mergeIn(
                                                                    requestedServiceData.getSecretConfig().toJson());
                                module.setSecretConfig(secretConfig);
                                return this.verticle.getEntityHandler()
                                                    .processDeploymentTransaction(module, EventAction.PATCH);
                            });
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(BIOS_SECRET.getEvents()));
    }

}
