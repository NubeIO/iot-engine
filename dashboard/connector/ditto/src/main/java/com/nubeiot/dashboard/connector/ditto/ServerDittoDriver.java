package com.nubeiot.dashboard.connector.ditto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.nubeiot.core.common.constants.Port;
import com.nubeiot.core.common.constants.Services;
import com.nubeiot.core.common.utils.Runner;
import com.nubeiot.core.common.utils.StringUtils;
import com.nubeiot.core.common.utils.response.ResponseUtils;
import com.nubeiot.core.common.RxMicroServiceVerticle;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;

/**
 * Created by topsykretts on 5/11/18.
 */
public class ServerDittoDriver extends RxMicroServiceVerticle {

    private static final String SERVER_DITTO_DRIVER = "io.nubespark.server.ditto.driver";

    private HttpClient client;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-server-ditto-driver/src/main/java/";
        Runner.runExample(JAVA_DIR, ServerDittoDriver.class);
    }

    @Override
    public void start(Future<Void> startFuture) {
        super.start();

        startWebApp()
            .flatMap(httpServer -> publishHttp())
            .flatMap(ignored -> publishMessageSource(SERVER_DITTO_DRIVER, SERVER_DITTO_DRIVER))
            .subscribe(record -> startFuture.complete(), startFuture::fail);


        client = vertx.createHttpClient(new HttpClientOptions()
            .setVerifyHost(false)
            .setTrustAll(true)
            .setTcpKeepAlive(true));
    }

    private Single<HttpServer> startWebApp() {
        // Create a router object.
        Router router = Router.router(vertx);
        router.route("/").handler(this::indexHandler);
        router.route().handler(BodyHandler.create());
        router.route("/*").handler(this::handleWebServer);
        // This is last handler that gives not found message
        router.route().last().handler(this::handlePageNotFound);

        // Create the HTTP server and pass the "accept" method to the request handler.
        return vertx.createHttpServer()
            .requestHandler(router::accept)
            .rxListen(appConfig.getInteger("http.port", Port.SERVER_DITTO_DRIVER_PORT))
            .doOnSuccess(httpServer -> logger.info("Ditto Server Driver started at port: " + httpServer.actualPort()))
            .doOnError(throwable -> logger.error("Cannot start Ditto Server Driver: " + throwable.getLocalizedMessage()));
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint("io.nubespark.server-ditto-driver", "0.0.0.0", appConfig.getInteger("http.port", Port.SERVER_DITTO_DRIVER_PORT))
            .doOnError(throwable -> logger.error("Cannot publish: " + throwable.getLocalizedMessage()));
    }

    private void handleWebServer(RoutingContext ctx) {
        logger.info("Proxying request: " + ctx.request().uri());
        requestDittoServer(client, ctx, dittoResHandler -> {
            JsonObject dittoRes = dittoResHandler.result();
            proxyDittoResponse(dittoRes, ctx);
        });
    }

    private void proxyDittoResponse(JsonObject dittoRes, RoutingContext ctx) {
        ctx.response().setChunked(true);
        JsonObject headers = dittoRes.getJsonObject("headers");
        Map<String, String> headerMap = new HashMap<>();
        for (String header : headers.fieldNames()) {
            headerMap.put(header, headers.getString(header));
        }
        ctx.response()
            .headers().setAll(new MultiMap(new VertxHttpHeaders().addAll(headerMap)));
        ctx.response().setStatusCode(dittoRes.getInteger("statusCode"));
        byte[] responseBody = dittoRes.getBinary("body");
        if (responseBody != null) {
            ctx.response().write(new String(responseBody, StandardCharsets.UTF_8));
        }
        ctx.response().end();
    }

    private void requestDittoServer(HttpClient client, RoutingContext ctx, Handler<AsyncResult<JsonObject>> next) {
        String uri = ctx.request().uri();
        HttpMethod httpMethod = ctx.request().method();
        String host = appConfig.getString("ditto.http.host", "localhost");
        Integer port = appConfig.getInteger("ditto.http.port", 8080);
        boolean ssl = false;
        if (port == 443 || port == 8443) {
            ssl = true;
        }

        HttpClientRequest req = client.request(httpMethod,
            new RequestOptions()
                .setHost(host)
                .setPort(port)
                .setURI(uri)
                .setSsl(ssl));

        req
            .toFlowable()
            .subscribe(res -> {
                logger.info("Proxying response: " + res.statusCode());
                JsonObject response = new JsonObject();
                response.put("statusCode", res.statusCode());
                JsonObject headers = new JsonObject();
                for (Map.Entry<String, String> entry : res.getDelegate().headers()) {
                    headers.put(entry.getKey(), entry.getValue());
                }
                response.put("headers", headers);

                Buffer data = new BufferImpl();
                res.handler(x -> data.appendBytes(x.getDelegate().getBytes()));
                res.endHandler((v) -> {
                    response.put("body", data.getBytes());
                    logger.info("Proxy Response Completed.");
                    next.handle(Future.succeededFuture(response));
                });
            });

        req.setChunked(true);
        //Adding ditto authorization
        if (appConfig.getBoolean("ditto-policy")) {
            req.putHeader(HttpHeaders.AUTHORIZATION.toString(), ctx.request().headers().get(HttpHeaders.AUTHORIZATION.toString()));
            if (StringUtils.isNotNull(ctx.getBody().toString())) {
                if (StringUtils.isNull(uri.replaceAll("/api/2/things/[^/]*(/)?", ""))) {
                    // This means we are we are PUTing device value for the first time or going to updated whole data
                    JsonObject body = ctx.getBodyAsJson();
                    body.put("policyId", Services.POLICY_NAMESPACE_PREFIX + ":" + new JsonObject(ctx.request().headers().getDelegate().get("user")).getString("site_id"));
                    logger.info("Body ::: " + body);
                    req.write(body.toString());
                } else {
                    req.write(ctx.getBody());
                }
            }
        } else {
            req.putHeader(HttpHeaders.AUTHORIZATION.toString(), "Basic " + getAuthKey());
            if (StringUtils.isNotNull(ctx.getBody().toString())) {
                req.write(ctx.getBody());
            }
        }
        req.end();
    }

    private String getAuthKey() {
        String apiKey = appConfig.getString("ditto.http.username", "ditto");
        String secretKey = appConfig.getString("ditto.http.password", "ditto");
        String auth = apiKey + ":" + secretKey;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    private void handlePageNotFound(RoutingContext routingContext) {
        String uri = routingContext.request().absoluteURI();
        routingContext.response()
            .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
            .setStatusCode(404)
            .end(Json.encodePrettily(new JsonObject()
                .put("uri", uri)
                .put("status", 404)
                .put("message", "Resource Not Found")
            ));
    }

    private void indexHandler(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(new JsonObject()
                .put("name", "server-ditto-driver")
                .put("version", "1.0")
                .put("vert.x_version", "3.4.1")
                .put("java_version", "8.0")
            ));
    }

}
