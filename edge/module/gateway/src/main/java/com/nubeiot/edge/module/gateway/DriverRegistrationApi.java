package com.nubeiot.edge.module.gateway;

import java.util.UUID;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.micro.HttpRecord;
import com.nubeiot.core.micro.providers.RestMicroContextProvider;

public class DriverRegistrationApi implements RestApi {

    @GET
    @Path("/drivers")
    public Future<ResponseData> getDrivers(@Context RoutingContext ctx,
                                           @Context RestMicroContextProvider microContextProvider) {
        Future<ResponseData> future = Future.future();

        microContextProvider.getMicroContext()
                            .getLocalController()
                            .getRecord()
                            .flatMap(records -> Observable.fromIterable(records).map(Record::toJson).toList())
                            .subscribe(records -> future.complete(ResponseDataWriter.responseData(
                                new JsonObject().put("records", new JsonArray(records.toString())).encode())));
        return future;
    }

    @POST
    @Path("/drivers/registration")
    public Future<ResponseData> driverRegistration(@Context RoutingContext ctx,
                                                   @Context RestMicroContextProvider microContextProvider) {
        Future<ResponseData> future = Future.future();
        JsonObject body = ctx.getBodyAsJson();
        HttpRecord httpRecord = HttpRecord.builder()
                                          .name(body.getString("name", UUID.randomUUID().toString()))
                                          .ssl(body.getBoolean("ssl", false))
                                          .host(body.getString("host", "0.0.0.0"))
                                          .port(body.getInteger("port", 8080))
                                          .root(body.getString("rootApi", ""))
                                          .build();

        microContextProvider.getMicroContext()
                            .getLocalController()
                            .addHttpRecord(httpRecord)
                            .subscribe(r -> future.complete(ResponseDataWriter.responseData(r.toJson().encode())));
        return future;
    }

    @DELETE
    @Path("/drivers/registration/:registration")
    public Future<ResponseData> deleteDriverRegistration(@Context RoutingContext ctx,
                                                         @Context RestMicroContextProvider microContextProvider) {
        Future<ResponseData> future = Future.future();
        String registration = ctx.request().getParam("registration");
        microContextProvider.getMicroContext()
                            .getLocalController()
                            .removeRecord(registration)
                            .subscribe(() -> future.complete(ResponseDataWriter.responseData(new JsonObject().encode())
                                                                               .setStatus(
                                                                                   HttpResponseStatus.NO_CONTENT)),
                                       e -> future.complete(ResponseDataConverter.convert(e)));

        return future;
    }

}
