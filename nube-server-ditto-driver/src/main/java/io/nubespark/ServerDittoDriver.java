package io.nubespark;

import static io.nubespark.constants.Port.SERVER_DITTO_DRIVER_PORT;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.nubespark.utils.Runner;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
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
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.core.http.WebSocket;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.servicediscovery.types.MessageSource;
import io.vertx.servicediscovery.Record;


/**
 * Created by topsykretts on 5/11/18.
 */
public class ServerDittoDriver extends RxMicroServiceVerticle {

    private static final String SERVER_DITTO_DRIVER = "io.nubespark.server.ditto.driver";
    private static final String DITTO_EVENTS = "io.nubespark.ditto.events";

    private WebSocket dittoWebSocket;
    private HttpClient client;
    private long failedTs = new Date().getTime();

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
            .flatMap(ignored -> publishMessageSource(DITTO_EVENTS, DITTO_EVENTS))
            .subscribe(record -> startFuture.complete(), startFuture::fail);


        client = vertx.createHttpClient(new HttpClientOptions()
            .setVerifyHost(false)
            .setTrustAll(true)
            .setTcpKeepAlive(true));

        handleDittoWebSocket(client);

        // This is message received from Edge ditto driver
        MessageSource.<JsonObject>getConsumer(discovery, new JsonObject().put("name", SERVER_DITTO_DRIVER), ar -> {
            if (ar.failed()) {
                logger.error("Message source {} is not discovered.", SERVER_DITTO_DRIVER);
            } else {
                MessageConsumer<JsonObject> consumer = ar.result();
                consumer.handler(message -> {
                    logger.info("Received message:: " + Json.encodePrettily(message.body()));
                    // Request ditto server and send response ...
                    requestDittoServer(client, message.body(), dittoResHandler -> {
                        JsonObject dittoResponse = dittoResHandler.result();
                        message.reply(dittoResponse);
                    });
                });
            }
        });
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
            .rxListen(config().getInteger("http.port", SERVER_DITTO_DRIVER_PORT))
            .doOnSuccess(httpServer -> logger.info("Ditto Server Driver started at port: " + httpServer.actualPort()))
            .doOnError(throwable -> logger.error("Cannot start Ditto Server Driver: " + throwable.getLocalizedMessage()));
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint("io.nubespark.server-ditto-driver", "0.0.0.0", config().getInteger("http.port", SERVER_DITTO_DRIVER_PORT))
            .doOnError(throwable -> logger.error("Cannot publish: " + throwable.getLocalizedMessage()));
    }

    private void handleWebServer(RoutingContext ctx) {
        JsonObject request = new JsonObject();
        request.put("method", ctx.request().method());
        request.put("uri", ctx.request().uri());
        if (ctx.getBody() != null) {
            request.put("body", ctx.getBody().toString());
        }

        logger.info("Proxying request: " + ctx.request().uri());
        requestDittoServer(client, request, dittoResHandler -> {
            JsonObject dittoRes = dittoResHandler.result();
            proxyDittoResponse(dittoRes, ctx);
        });
    }

    private void handleDittoWebSocket(HttpClient client) {
        String host = config().getString("ditto.http.host", "localhost");
        Integer port = config().getInteger("ditto.http.port", SERVER_DITTO_DRIVER_PORT);

        // Subscribe to ditto events and make it available to vertx event bus
        RequestOptions requestOptions = new RequestOptions()
            .setHost(host)
            .setPort(port)
            .setURI("/ws/2");
        if (port == 443 || port == 8443 || config().getBoolean("ditto.ssl", false)) {
            requestOptions.setSsl(true);
        }

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION.toString(), "Basic " + getAuthKey());

        client.websocket(
            requestOptions,
            new MultiMap(new VertxHttpHeaders().addAll(headers)),
            this::handleWebSocketSuccess,
            error -> {
                // Reconnects on every 5 seconds on failure...
                vertx.setTimer(5000, l -> handleDittoWebSocket(client));
                if (failedTs != 0) {
                    long now = new Date().getTime();
                    long diff = now - failedTs;
                    logger.info("Ditto WebSocket is disconnected for: " + diff);
                }
                logger.info("Connection to webSocket failed.");
                logger.info(error.getMessage());
                logger.info("Retrying to connect...");
                error.printStackTrace();
            });
    }

    private void handleWebSocketSuccess(WebSocket webSocket) {
        logger.info("WebSocket connection established");
        dittoWebSocket = webSocket;
        dittoWebSocket.handler(data -> {
            if (data.toString("ISO-8859-1").endsWith("ACK")) {
                logger.info("Received ack ditto::: " + data.toString("ISO-8859-1"));
            } else if (!data.toString("ISO-8859-1").equals("")) {
                vertx.eventBus().publish(DITTO_EVENTS, data.toJsonObject());
            }
        });

        dittoWebSocket.writeTextMessage("START-SEND-EVENTS");

        dittoWebSocket.exceptionHandler(handler -> logger.error(handler.getMessage()));

        dittoWebSocket.closeHandler(handler -> {
            logger.warn("Websocket connection has been closed...");
            failedTs = new Date().getTime();
            // Reconnects on every 5 seconds on Websocket close...
            vertx.setTimer(5000, l -> handleDittoWebSocket(client));
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

    private void requestDittoServer(HttpClient client, JsonObject message, Handler<AsyncResult<JsonObject>> next) {
        String uri = message.getString("uri"); // resource of ditto
        String method = message.getString("method"); // request method Eg: GET, PUT, POST, DELETE, etc.
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        String host = config().getString("ditto.http.host", "localhost");
        Integer port = config().getInteger("ditto.http.port", 8080);
        boolean ssl = false;
        if (port == 443 || port == 8443) {
            ssl = true;
        }
        Buffer body = null;
        if (message.fieldNames().contains("body")) {
            body = Buffer.buffer(message.getString("body"));
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
        req.putHeader(HttpHeaders.AUTHORIZATION.toString(), "Basic " + getAuthKey());
        if (body != null) {
            req.write(body.toString());
        }
        req.end();
    }

    private String getAuthKey() {
        String apiKey = config().getString("ditto.http.username", "ditto");
        String secretKey = config().getString("ditto.http.password", "ditto");
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

    @Override
    public void stop(Future<Void> future) {
        super.stop(future);
        if (dittoWebSocket != null) {
            logger.info("Verticle is stopping... Un-subscribing from ditto events");
            dittoWebSocket.writeTextMessage("STOP-SEND-EVENTS");
            dittoWebSocket.close();
        } else {
            logger.info("Ditto WebSocket it null. Fix it");
        }
    }

}
