package com.nubeiot.edge.module.gateway.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.gateway.EdgeGatewayVerticle;

import lombok.Getter;
import lombok.NonNull;

public class DriverRegistrationEventListener implements EventListener {

    @Getter
    private final List<EventAction> availableEvents;
    private final EdgeGatewayVerticle verticle;

    public DriverRegistrationEventListener(EdgeGatewayVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    private Function<Record, Boolean> defaultHttpEndpointFilter(HttpLocation location, String serviceName) {
        return r -> location.getHost().equals(r.getLocation().getString("host")) &&
                    location.getPort() == r.getLocation().getInteger("port") &&
                    location.getRoot().equals(r.getLocation().getString("root")) && r.getName().equals(serviceName);
    }

    private Single<Record> createRecord(MicroContext microContext, String serviceName, HttpLocation location,
                                        JsonObject metadata) {
        return microContext.getLocalController().addHttpRecord(serviceName, location, metadata);
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        return verticle.getMicroContext()
                       .getLocalController()
                       .getRecords()
                       .flatMap(records -> Observable.fromIterable(records).map(Record::toJson).toList())
                       .map(records -> new JsonObject().put("records", new JsonArray(records)));
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(@Param("data") JsonObject data) {
        HttpLocation location = JsonData.convertLenient(data, HttpLocation.class);
        String serviceName = data.getString("name");
        JsonObject metadata = data.getJsonObject("metadata");
        Networks.validPort(location.getPort());
        if (Strings.isBlank(data.getString("name"))) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "name should not be blank");
        }
        if (!Urls.validateHost(location.getHost())) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid host");
        }
        return verticle.getMicroContext()
                       .getLocalController()
                       .contains(defaultHttpEndpointFilter(location, serviceName), HttpEndpoint.TYPE)
                       .flatMap(existed -> {
                           if (existed) {
                               throw new AlreadyExistException("Service is already registered");
                           }
                           return createRecord(verticle.getMicroContext(), serviceName, location, metadata);
                       })
                       .map(Record::toJson);
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> remove(@Param("registration") String registration) {
        return verticle.getMicroContext()
                       .getLocalController()
                       .removeRecord(registration)
                       .andThen(Single.just(new JsonObject()));
    }

}
