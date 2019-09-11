package com.nubeiot.edge.module.gateway;

import java.util.Collection;
import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class RouterRegistrationListener implements EventListener {

    private final MicroContext microContext;
    @Getter
    private final Collection<EventAction> availableEvents;

    private Function<Record, Boolean> defaultHttpEndpointFilter(HttpLocation location, String serviceName) {
        return r -> location.getHost().equals(r.getLocation().getString("host")) &&
                    location.getPort() == r.getLocation().getInteger("port") &&
                    location.getRoot().equals(r.getLocation().getString("root")) && r.getName().equals(serviceName);
    }

    private Single<Record> createRecord(MicroContext microContext, String serviceName, HttpLocation location,
                                        JsonObject metadata) {
        return microContext.getLocalController().addHttpRecord(serviceName, location, metadata);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        JsonObject data = requestData.body();
        HttpLocation location = JsonData.convertLenient(data, HttpLocation.class);
        String serviceName = data.getString("name");
        JsonObject metadata = data.getJsonObject("metadata");
        Networks.validPort(location.getPort());
        if (Strings.isBlank(data.getString("name"))) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Service name is mandatory");
        }
        if (!Urls.validateHost(location.getHost())) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid host");
        }
        return microContext.getLocalController()
                           .contains(defaultHttpEndpointFilter(location, serviceName), HttpEndpoint.TYPE)
                           .flatMap(existed -> {
                               if (existed) {
                                   throw new AlreadyExistException("Service is already registered");
                               }
                               return createRecord(microContext, serviceName, location, metadata);
                           })
                           .map(Record::toJson);
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> remove(RequestData requestData) {
        String registration = requestData.body().getString("registration");
        return microContext.getLocalController().removeRecord(registration).andThen(Single.just(new JsonObject()));
    }

}
