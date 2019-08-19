package com.nubeiot.dashboard.connector.ditto;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import java.util.Base64;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Builder;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.HttpConfig;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestConfigProvider;
import com.nubeiot.core.utils.Strings;
import com.zandero.rest.annotation.ResponseWriter;

public class ServerDittoRestController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(ServerDittoRestController.class);

    @GET
    @Path("/api/*")
    @ResponseWriter(ResponseDataWriter.class)
    public Future<ResponseData> dittoGet(@Context Vertx vertx, @Context RoutingContext ctx,
                                         @Context RestConfigProvider config) {
        return dittoRequestDispatcher(vertx, ctx, config.getConfig());
    }

    @POST
    @Path("/api/*")
    public Future<ResponseData> dittoPost(@Context Vertx vertx, @Context RoutingContext ctx,
                                          @Context RestConfigProvider config) {
        return dittoRequestDispatcher(vertx, ctx, config.getConfig());
    }

    @PUT
    @Path("/api/*")
    public Future<ResponseData> dittoPut(@Context Vertx vertx, @Context RoutingContext ctx,
                                         @Context RestConfigProvider config) {
        return dittoRequestDispatcher(vertx, ctx, config.getConfig());
    }

    @DELETE
    @Path("/api/*")
    public Future<ResponseData> dittoDelete(@Context Vertx vertx, @Context RoutingContext ctx,
                                            @Context RestConfigProvider config) {
        return dittoRequestDispatcher(vertx, ctx, config.getConfig());
    }

    private Future<ResponseData> dittoRequestDispatcher(Vertx vertx, RoutingContext ctx, NubeConfig config) {
        Future<ResponseData> future = Future.future();
        DittoConfig dittoConfig = IConfig.from(config.getAppConfig(), DittoConfig.class);
        HttpConfig httpConfig = IConfig.from(config.getAppConfig(), HttpConfig.class);

        // Getting actual Ditto call API
        String uri = ctx.request().uri().replaceFirst(httpConfig.getRestConfig().getRootApi(), "");
        logger.info("Proxying request: {}", uri);

        HttpMethod httpMethod = ctx.request().method();
        int port = dittoConfig.getPort();
        RequestOptions requestOptions = new RequestOptions().setHost(dittoConfig.getHost())
                                                            .setPort(port)
                                                            .setSsl(port == 443);

        HttpClientDelegate client = HttpClientDelegate.create(vertx, HostInfo.from(requestOptions));
        Builder requestDataBuilder = RequestData.builder();

        if (dittoConfig.getPolicy()) {
            requestDataBuilder.headers(new JsonObject().put(AUTHORIZATION, ctx.request().headers().get(AUTHORIZATION)));
            if (Strings.isNotBlank(ctx.getBody().toString())) {
                if (Strings.isBlank(uri.replaceAll("/api/2/things/[^/]*(/)?", ""))) {
                    // This means we are we are PUTing device value for the first time or going to updated whole data
                    JsonObject body = ctx.getBodyAsJson();
                    body.put("policyId", dittoConfig.getPrefix() + ":" +
                                         new JsonObject(ctx.request().headers().get("User")).getString("site_id"));
                    requestDataBuilder.body(body);
                } else {
                    requestDataBuilder.body(ctx.getBodyAsJson());
                }
            }
        } else {
            String authorization = "Basic " + getAuthKey(dittoConfig.getUsername(), dittoConfig.getPassword());
            requestDataBuilder.headers(new JsonObject().put(AUTHORIZATION, authorization));
            if (Strings.isNotBlank(ctx.getBody().toString())) {
                requestDataBuilder.body(new JsonObject(ctx.getBody().toString()));
            }
        }
        client.execute(uri, httpMethod, requestDataBuilder.build()).subscribe(data -> {
            future.complete(ResponseDataWriter.serializeResponseData(data));
            client.close();
        }, err -> future.complete(ResponseDataConverter.convert(err)));
        return future;
    }

    private String getAuthKey(String username, String password) {
        String auth = username + ":" + password;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

}
