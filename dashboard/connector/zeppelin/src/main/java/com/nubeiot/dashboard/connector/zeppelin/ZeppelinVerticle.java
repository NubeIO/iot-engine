package com.nubeiot.dashboard.connector.zeppelin;

import com.nubeiot.core.common.RxMicroServiceVerticle;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import com.nubeiot.core.common.constants.Port;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpClientResponse;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ZeppelinVerticle extends RxMicroServiceVerticle {

    private HttpClient client;
    private String COOKIE_NAME = "JSESSIONID";

    @Override
    public void start(Future<Void> future) {
        Future<Void> startFuture = Future.future();
        super.start(startFuture);
        startFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                client = vertx.createHttpClient(new HttpClientOptions());
                Router router = Router.router(vertx);
                // creating body handler
                router.route().handler(BodyHandler.create());
                handleRoutes(router);

                vertx.createHttpServer()
                     .requestHandler(router)
                     .rxListen(appConfig.getInteger("http.port", Port.ZEPPELIN_PORT))
                     .doOnSuccess(httpServer -> logger.info("Web server started at " + httpServer.actualPort()))
                     .doOnError(throwable -> logger.error("Cannot start server: " + throwable.getLocalizedMessage()))
                     .flatMap(httpServer -> publishHttp())
                     .subscribe(record -> future.complete(), future::fail);
            } else {
                logger.info("Failure on deployment...");
                startFuture.fail(ar.cause());
            }
        });
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint("zeppelin-api", "0.0.0.0", appConfig.getInteger("http.port", Port.ZEPPELIN_PORT))
                .doOnError(throwable -> logger.error("Cannot publish: " + throwable.getLocalizedMessage()));
    }

    private void handleRoutes(Router router) {
        // api dispatcher
        router.route("/api/*").handler(this::dispatchRequests);
    }

    private void dispatchRequests(RoutingContext context) {
        HttpClientRequest toReq = client.request(context.request().method(),
            appConfig.getInteger("server.port"), appConfig.getString("server.host"), context.request().uri(), response -> {
                response.bodyHandler(body -> {
                    HttpServerResponse toRsp = context.response().setStatusCode(response.statusCode());
                    if (response.statusCode() < 500) {
                        response.headers().getDelegate().forEach(header -> {
                            if (!header.getKey().equalsIgnoreCase("Set-Cookie")) {
                                // Ignore cookies; on login it will send cookie
                                toRsp.putHeader(header.getKey(), header.getValue());
                            }
                        });
                        body = cookieHandler(response, toRsp, body);
                    }
                    toRsp.end(body);
                });
            });
        // set headers
        context.request().headers().getDelegate().forEach(header -> {
            if (header.getKey().equalsIgnoreCase(COOKIE_NAME)) {
                // Sending as an cookie for authentication parameter
                toReq.putHeader("Cookie", header.getValue());
            } else {
                toReq.putHeader(header.getKey(), header.getValue());
            }
        });

        if (context.getBody() == null) {
            toReq.end();
        } else {
            toReq.end(context.getBody());
        }
    }

    private Buffer cookieHandler(HttpClientResponse response, HttpServerResponse toRsp, Buffer body) {
        final Map<String, String> cookie = new HashMap<>();
        response.headers().getDelegate().forEach(header -> {
            if (header.getKey().equalsIgnoreCase("Set-Cookie")) {
                if (header.getValue().contains(COOKIE_NAME)) {
                    // Two JSESSIONID= will be available and we need the last one
                    cookie.put(COOKIE_NAME, (header.getValue().split(";")[0]).split("=")[1]);
                }
            }
        });
        if (body.length() != 0) {
            System.out.println(new JsonObject(body.getDelegate()).toString());
            if (cookie.keySet().contains(COOKIE_NAME)) {
                JsonObject responseJsonObject = new JsonObject(body.getDelegate());
                JsonObject body$ = responseJsonObject.getJsonObject("body").put(COOKIE_NAME, cookie.get(COOKIE_NAME));
                responseJsonObject = responseJsonObject.put("body", body$);
                body = Buffer.buffer(responseJsonObject.toString());
                toRsp.putHeader("Content-Length", Integer.toString(body.toString().toCharArray().length));
            }
        }
        return body;
    }
}
