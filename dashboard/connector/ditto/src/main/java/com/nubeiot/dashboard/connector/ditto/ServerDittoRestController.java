package com.nubeiot.dashboard.connector.ditto;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.github.zero.utils.Strings;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.auth.BasicCredential;
import com.nubeiot.auth.CredentialType;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Builder;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.HttpConfig;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientConfig;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestHttpClientConfigProvider;
import com.nubeiot.core.http.rest.provider.RestHttpConfigProvider;
import com.zandero.rest.annotation.ResponseWriter;

public class ServerDittoRestController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(ServerDittoRestController.class);

    @GET
    @Path("/api/*")
    @ResponseWriter(ResponseDataWriter.class)
    public Future<ResponseData> dittoGet(@Context Vertx vertx, @Context RoutingContext ctx,
                                         @Context RestHttpConfigProvider httpConfigProvider,
                                         @Context RestHttpClientConfigProvider httpClientConfigProvider,
                                         @Context RestDittoConfigProvider dittoConfigProvider) {
        return dittoRequestDispatcher(vertx, ctx, httpConfigProvider.getHttpConfig(),
                                      dittoConfigProvider.getDittoConfig(),
                                      httpClientConfigProvider.getHttpClientConfig());
    }

    @POST
    @Path("/api/*")
    public Future<ResponseData> dittoPost(@Context Vertx vertx, @Context RoutingContext ctx,
                                          @Context RestHttpConfigProvider httpConfigProvider,
                                          @Context RestHttpClientConfigProvider httpClientConfigProvider,
                                          @Context RestDittoConfigProvider dittoConfigProvider) {
        return dittoRequestDispatcher(vertx, ctx, httpConfigProvider.getHttpConfig(),
                                      dittoConfigProvider.getDittoConfig(),
                                      httpClientConfigProvider.getHttpClientConfig());
    }

    @PUT
    @Path("/api/*")
    public Future<ResponseData> dittoPut(@Context Vertx vertx, @Context RoutingContext ctx,
                                         @Context RestHttpConfigProvider httpConfigProvider,
                                         @Context RestHttpClientConfigProvider httpClientConfigProvider,
                                         @Context RestDittoConfigProvider dittoConfigProvider) {
        return dittoRequestDispatcher(vertx, ctx, httpConfigProvider.getHttpConfig(),
                                      dittoConfigProvider.getDittoConfig(),
                                      httpClientConfigProvider.getHttpClientConfig());
    }

    @DELETE
    @Path("/api/*")
    public Future<ResponseData> dittoDelete(@Context Vertx vertx, @Context RoutingContext ctx,
                                            @Context RestHttpConfigProvider httpConfigProvider,
                                            @Context RestHttpClientConfigProvider httpClientConfigProvider,
                                            @Context RestDittoConfigProvider dittoConfigProvider) {
        return dittoRequestDispatcher(vertx, ctx, httpConfigProvider.getHttpConfig(),
                                      dittoConfigProvider.getDittoConfig(),
                                      httpClientConfigProvider.getHttpClientConfig());
    }

    private Future<ResponseData> dittoRequestDispatcher(Vertx vertx, RoutingContext ctx, HttpConfig httpConfig,
                                                        DittoConfig dittoConfig, HttpClientConfig httpClientConfig) {
        Future<ResponseData> future = Future.future();

        // Getting actual Ditto call API
        String uri = ctx.request().uri().replaceFirst(httpConfig.getRestConfig().getRootApi(), "");
        logger.info("Proxying request: {}", uri);

        HttpMethod httpMethod = ctx.request().method();
        int port = dittoConfig.getPort();
        RequestOptions requestOptions = new RequestOptions().setHost(dittoConfig.getHost())
                                                            .setPort(port)
                                                            .setSsl(port == 443);

        HttpClientDelegate client = HttpClientDelegate.create(vertx, httpClientConfig, HostInfo.from(requestOptions));
        Builder requestDataBuilder = RequestData.builder();

        if (dittoConfig.getPolicy()) {
            requestDataBuilder.headers(new JsonObject().put(HttpHeaders.AUTHORIZATION.toString(),
                                                            ctx.request().headers().get(HttpHeaders.AUTHORIZATION)));
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
            BasicCredential basicCredential = new BasicCredential(CredentialType.BASIC, dittoConfig.getUsername(),
                                                                  dittoConfig.getPassword());
            requestDataBuilder.headers(
                new JsonObject().put(HttpHeaders.AUTHORIZATION.toString(), basicCredential.toHeader()));
            if (Strings.isNotBlank(ctx.getBody().toString())) {
                requestDataBuilder.body(new JsonObject(ctx.getBody().toString()));
            }
        }
        client.execute(uri, httpMethod, requestDataBuilder.build())
              .subscribe(data -> future.complete(ResponseDataWriter.serializeResponseData(data)),
                         err -> future.complete(ResponseDataConverter.convert(err)));
        return future;
    }

}
