package com.nubeiot.dashboard.connector.mist;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.HttpConfig;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestConfigProvider;
import com.nubeiot.core.utils.Strings;
import com.zandero.rest.annotation.ResponseWriter;

public class MistRestController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(MistRestController.class);

    @GET
    @Path("/info")
    public JsonObject info() {
        return new JsonObject().put("name", "mist-proxy-server").put("version", "1.0.0").put("java_version", "8.0");
    }

    @GET
    @Path("/v2/api/functions/*")
    @ResponseWriter(ResponseDataWriter.class)
    public Future<ResponseData> getFunction(@Context Vertx vertx, @Context RoutingContext ctx,
                                            @Context RestConfigProvider config) {
        return dittoRequestDispatcher(vertx, ctx, config.getConfig());
    }

    @POST
    @Path("/v2/api/functions/*")
    @ResponseWriter(ResponseDataWriter.class)
    public Future<ResponseData> postFunction(@Context Vertx vertx, @Context RoutingContext ctx,
                                             @Context RestConfigProvider config) {
        return dittoRequestDispatcher(vertx, ctx, config.getConfig());
    }

    private Future<ResponseData> dittoRequestDispatcher(Vertx vertx, RoutingContext ctx, NubeConfig config) {
        Future<ResponseData> future = Future.future();
        HttpClient client = vertx.createHttpClient(
            new HttpClientOptions().setVerifyHost(false).setTrustAll(true).setTcpKeepAlive(true));
        MistConfig mistConfig = IConfig.from(config.getAppConfig(), MistConfig.class);
        HttpConfig httpConfig = IConfig.from(config.getAppConfig(), HttpConfig.class);

        // Getting actual Mist call API
        String uri = ctx.request().uri().replaceFirst(httpConfig.getRestConfig().getRootApi(), "");
        logger.info("Proxying request: {}", uri);

        HttpMethod httpMethod = ctx.request().method();
        String schema = mistConfig.getSchema();
        String host = mistConfig.getHost();
        int port = mistConfig.getPort();
        boolean ssl = false;
        if (schema.equals("https")) {
            ssl = true;
        }
        HttpClientRequest req = client.request(httpMethod, new RequestOptions().setHost(host)
                                                                               .setPort(port)
                                                                               .setURI(uri)
                                                                               .setSsl(ssl));
        req.handler(res -> {
            logger.info("Proxying Response StatusCode: {}", res.statusCode());
            res.bodyHandler(data -> {
                logger.info("Proxy Response Completed.");
                logger.info("Data is: " + JsonData.tryParse(data).toJson().encode());

                future.complete(
                    ResponseDataWriter.serializeResponseData(JsonData.tryParse(data, true).toJson().encode())
                                      .setStatus(res.statusCode()));
            });
        }).exceptionHandler(e -> future.complete(ResponseDataConverter.convert(e)));

        req.setChunked(true);
        if (Strings.isNotBlank(ctx.getBody().toString())) {
            req.write(ctx.getBody());
        }

        req.end();
        return future;
    }

}
