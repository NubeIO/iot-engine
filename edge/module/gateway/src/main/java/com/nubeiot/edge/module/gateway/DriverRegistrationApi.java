package com.nubeiot.edge.module.gateway;

import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestMicroContextProvider;

public class DriverRegistrationApi implements RestApi {

    @GET
    @Path("/drivers")
    public Future<ResponseData> getDrivers(@Context RoutingContext ctx,
                                           @Context RestMicroContextProvider microContextProvider) {
        Future<ResponseData> future = Future.future();

        microContextProvider.getMicroContext().getLocalController().getRecords()
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
        int port;
        if (body.containsKey("port")) {
            port = body.getInteger("port");
        } else {
            future.complete(ResponseDataConverter.convert(
                new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Port is mandatory field.")));
            return future;
        }

        microContextProvider.getMicroContext()
                            .getLocalController()
                            .getRecords()
                            .map(records -> records.stream()
                                                   .filter(r -> r.getLocation().containsKey("port") &&
                                                                r.getLocation().getInteger("port").equals(port))
                                                   .collect(Collectors.toList())
                                                   .size() > 0)
                            .flatMap(alreadyExist -> {
                                if (alreadyExist) {
                                    throw new AlreadyExistException("We have a service running on the given port.");
                                } else {
                                    return createRecord(microContextProvider, body, port);
                                }
                            })
                            .subscribe(r -> future.complete(ResponseDataWriter.responseData(r.toJson().encode())),
                                       e -> future.complete(ResponseDataConverter.convert(e)));

        return future;
    }

    private Single<Record> createRecord(@Context RestMicroContextProvider microContextProvider, JsonObject body,
                                        int port) {
        Record record = HttpEndpoint.createRecord(body.getString("name", UUID.randomUUID().toString()),
                                                  body.getBoolean("ssl", false), body.getString("host", "localhost"),
                                                  port, body.getString("rootApi", ""), new JsonObject());

        return microContextProvider.getMicroContext().getLocalController().addRecord(record);
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
