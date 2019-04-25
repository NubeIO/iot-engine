package com.nubeiot.edge.module.gateway;

import java.util.UUID;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.micro.HttpRecord;
import com.nubeiot.core.micro.providers.RestMicroContextProvider;

public class DriverRegistrationApi implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(DriverRegistrationApi.class);

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
                            .subscribe(
                                r -> future.complete(ResponseDataWriter.responseData(new JsonObject().encode())));
        return future;
    }

}
