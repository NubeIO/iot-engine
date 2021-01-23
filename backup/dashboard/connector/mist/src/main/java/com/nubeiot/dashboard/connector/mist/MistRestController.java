package com.nubeiot.dashboard.connector.mist;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.github.zero88.utils.Strings;
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
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.client.HttpClientConfig;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestNubeConfigProvider;

public class MistRestController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(MistRestController.class);

    @GET
    @Path("/v2/api/*")
    public Future<ResponseData> getFunction(@Context Vertx vertx, @Context RoutingContext ctx,
                                            @Context RestNubeConfigProvider config) {
        return mistRequestDispatcher(vertx, ctx, config.getNubeConfig());
    }

    @POST
    @Path("/v2/api/*")
    public Future<ResponseData> postFunction(@Context Vertx vertx, @Context RoutingContext ctx,
                                             @Context RestNubeConfigProvider config) {
        return mistRequestDispatcher(vertx, ctx, config.getNubeConfig());
    }

    private Future<ResponseData> mistRequestDispatcher(Vertx vertx, RoutingContext ctx, NubeConfig config) {
        Future<ResponseData> future = Future.future();
        MistConfig mistConfig = IConfig.from(config.getAppConfig(), MistConfig.class);
        HttpConfig httpConfig = IConfig.from(config.getAppConfig(), HttpConfig.class);

        // Getting actual Mist call API
        String uri = ctx.request().uri().replaceFirst(httpConfig.getRestConfig().getRootApi(), "");
        logger.trace("Proxying request: {}", uri);

        HttpMethod httpMethod = ctx.request().method();
        RequestOptions requestOptions = new RequestOptions().setHost(mistConfig.getHost())
                                                            .setPort(mistConfig.getPort())
                                                            .setSsl(mistConfig.getSchema().equals("https"));
        HttpClientConfig httpClientConfig = IConfig.from(config.getAppConfig(), HttpClientConfig.class);
        logger.info("Http Client config: {}", httpClientConfig.toJson());

        HttpClientDelegate client = HttpClientDelegate.create(vertx, httpClientConfig, HostInfo.from(requestOptions));
        Builder requestDataBuilder = RequestData.builder();
        if (Strings.isNotBlank(ctx.getBody().toString())) {
            requestDataBuilder.body(new JsonObject(ctx.getBody().toString()));
        }
        requestDataBuilder.headers(HttpUtils.HttpHeaderUtils.serializeHeaders(ctx.request().headers()));
        client.execute(uri, httpMethod, requestDataBuilder.build())
              .subscribe(data -> future.complete(ResponseDataWriter.serializeResponseData(data)),
                         err -> future.complete(ResponseDataConverter.convert(err)));
        return future;
    }

}
