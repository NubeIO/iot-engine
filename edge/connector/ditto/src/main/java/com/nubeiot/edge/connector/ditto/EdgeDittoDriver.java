package com.nubeiot.edge.connector.ditto;

import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.Address;
import com.nubeiot.core.common.RxMicroServiceVerticle;
import com.nubeiot.core.common.constants.Port;
import com.nubeiot.core.common.utils.Runner;
import com.nubeiot.core.common.utils.response.ResponseUtils;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
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
 * This EdgeDittoDriver resides on Edge Devices, and have following functionalities:
 * 1. Can PUT some events on NodeRED (Currently we have no any use cases).
 * 1.1. ServerDittoDriver can PUT some events to NodeRED
 * 1.2. Other micro-services on Edge Device can communicate to EdgeDittoDriver for PUTing some events
 * <p>
 * 2. CURD operation request from EdgeDittoDriver to ServerDittoDriver
 */
public class EdgeDittoDriver extends RxMicroServiceVerticle {

    private static final String EDGE_DITTO_DRIVER = "io.nubespark.edge.ditto.driver";
    private static final String SERVER_DITTO_DRIVER = "io.nubespark.server.ditto.driver";

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-edge-ditto-driver/src/main/java/";
        Runner.runExample(JAVA_DIR, EdgeDittoDriver.class);
    }

    @Override
    public void start(Future<Void> startFuture) {
        super.start();
        String nodeRedHost = config().getString("nodered.host", "localhost");
        Integer nodeRedPort = config().getInteger("nodered.port", 1880);
        logger.info("NodeRED Host: " + nodeRedHost);
        logger.info("NodeRED Port: " + nodeRedPort);

        HttpClient client = vertx.createHttpClient(new HttpClientOptions());

        // From Edge Devices to NodeRED
        vertx.eventBus().<JsonObject>consumer(EDGE_DITTO_DRIVER)
            .toFlowable()
            .subscribe(message -> {
                JsonObject request = message.body();
                logger.info("Received request from server... ");
                HttpClientRequest req = client.request(HttpMethod.PUT,
                    nodeRedPort,
                    nodeRedHost,
                    "/device/manager",
                    res -> {
                        JsonObject response = new JsonObject();
                        response.put("statusCode", res.statusCode());
                        JsonObject headers = new JsonObject();
                        for (Map.Entry<String, String> entry : res.headers().getDelegate().entries()) {
                            headers.put(entry.getKey(), entry.getValue());
                        }
                        response.put("headers", headers);
                        res.handler(data -> response.put("body", data.getDelegate().getBytes()));
                        res.endHandler((v) -> {
                            logger.info("Got response from NodeRED. Sending it to server.");
                            message.reply(response);
                        });
                    });
                req.setChunked(true);
                // Adding ditto authorization
                req.write(Buffer.buffer(Json.encodePrettily(request)).toString());

                req.end();
                logger.info("Requesting NodeRED to handleEvent the request from server...");
            });

        this.startWebApp()
            .flatMap(httpServer -> publishHttp())
            .subscribe(record -> startFuture.complete(), startFuture::fail);
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint("io.nubespark.edge-ditto-driver", "0.0.0.0", config().getInteger("http.port", Port.EDGE_DITTO_DRIVER_PORT))
            .doOnError(throwable -> logger.error("Cannot publish: " + throwable.getLocalizedMessage()));
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
            .rxListen(config().getInteger("http.port", Port.EDGE_DITTO_DRIVER_PORT))
            .doOnSuccess(httpServer -> logger.info("Ditto Edge Driver started at port: " + httpServer.actualPort()))
            .doOnError(throwable -> logger.error("Cannot start Ditto Edge Driver: " + throwable.getLocalizedMessage()));
    }

    private void handleWebServer(RoutingContext ctx) {
        logger.info("Inside body handler...");
        JsonObject request = new JsonObject();
        request.put("method", ctx.request().method().toString());
        request.put("uri", ctx.request().uri());
        if (ctx.getBody() != null) {
            request.put("body", ctx.getBody().toString());
        }

        logger.info(Json.encodePrettily(request));
        logger.info("Sending response...");

        vertx.eventBus().<JsonObject>send(SERVER_DITTO_DRIVER, request, handler -> {
            if (handler.succeeded()) {
                JsonObject response = handler.result().body();
                if (ctx.request().method() != HttpMethod.GET) {
                    ctx.request().response().setChunked(true);
                }
                JsonObject headers = response.getJsonObject("headers");
                Map<String, String> headerMap = new HashMap<>();
                for (String header : headers.fieldNames()) {
                    headerMap.put(header, headers.getString(header));
                }
                ctx.request().response().headers().setAll(new MultiMap(new VertxHttpHeaders().addAll(headerMap)));
                ctx.request().response().setStatusCode(response.getInteger("statusCode"));
                byte[] responseBody = response.getBinary("body");
                if (responseBody != null) {
                    ctx.request().response().write(Buffer.buffer(responseBody).toString());
                }
                ctx.request().response().end();
            } else {
                // TODO: 5/12/18 Identify cases where request fails and handleEvent accordingly
                ctx.request().response()
                    .setStatusCode(500)
                    .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                    .end(Json.encodePrettily(new JsonObject()
                        .put("message", "Internal Server Error")
                        .put("error", handler.cause().getMessage())
                    ));
            }
            logger.info(" After sending response...");
        });
        logger.info("Outside body handler...");
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
                .put("name", "edge-ditto-driver")
                .put("version", "1.0")
                .put("vert.x_version", "3.4.1")
                .put("java_version", "8.0")
            ));
    }


    private String getDeviceAddress() {
        for (HazelcastInstance instance : Hazelcast.getAllHazelcastInstances()) {
            Address address = instance.getCluster().getLocalMember().getAddress();
            return address.getHost() + ":" + address.getPort();
        }
        return null;
    }
}
