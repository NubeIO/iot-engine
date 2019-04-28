package com.nubeiot.edge.module.gateway;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestMicroContextProvider;
import com.nubeiot.core.utils.Networks;

public class DriverRegistrationApi implements RestApi {

    @GET
    @Path("/drivers")
    public Future<JsonObject> getDrivers(@Context RoutingContext ctx,
                                         @Context RestMicroContextProvider microContextProvider) {
        Future<JsonObject> future = Future.future();
        microContextProvider.getMicroContext()
                            .getLocalController()
                            .getRecords()
                            .flatMap(records -> Observable.fromIterable(records).map(Record::toJson).toList())
                            .subscribe(
                                records -> future.complete(new JsonObject().put("records", new JsonArray(records))),
                                future::fail);
        return future;
    }

    @POST
    @Path("/drivers/registration")
    @Produces(HttpUtils.DEFAULT_CONTENT_TYPE)
    public Future<JsonObject> registerDriver(@Context RoutingContext ctx,
                                             @Context RestMicroContextProvider microContextProvider) {
        JsonObject body = ctx.getBodyAsJson();
        HttpLocation location = JsonData.convertLenient(body, HttpLocation.class);
        String serviceName = body.getString("name");
        JsonObject metadata = body.getJsonObject("metadata");
        Networks.validPort(location.getPort());
        Future<JsonObject> future = Future.future();
        microContextProvider.getMicroContext()
                            .getLocalController()
                            .contains(r -> location.getPort() == r.getLocation().getInteger("port"), HttpEndpoint.TYPE)
                            .flatMap(existed -> {
                                if (existed) {
                                    throw new AlreadyExistException("Service is already registered");
                                }
                                return createRecord(microContextProvider, serviceName, location, metadata);
                            })
                            .map(Record::toJson)
                            .subscribe(future::complete, future::fail);
        return future;
    }

    private Single<Record> createRecord(RestMicroContextProvider microContextProvider, String serviceName,
                                        HttpLocation location, JsonObject metadata) {
        return microContextProvider.getMicroContext()
                                   .getLocalController()
                                   .addHttpRecord(serviceName, location, metadata);
    }

    @DELETE
    @Path("/drivers/registration/:registration")
    public Future<ResponseData> unRegisterDriver(@Context RoutingContext ctx,
                                                 @Context RestMicroContextProvider microContextProvider) {
        Future<ResponseData> future = Future.future();
        String registration = ctx.request().getParam("registration");
        microContextProvider.getMicroContext()
                            .getLocalController()
                            .removeRecord(registration)
                            .subscribe(() -> future.complete(ResponseData.noContent()), future::fail);
        return future;
    }

}
