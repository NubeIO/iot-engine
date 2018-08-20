package io.nubespark.vertx.common;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An implementation of {@link Verticle} taking care of the discovery and publication of services.
 */
public class MicroServiceVerticle extends AbstractVerticle {
    Logger logger = LoggerFactory.getLogger(MicroServiceVerticle.class);
    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;
    protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

    @Override
    public void start() {

        // initializing service discovery
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

        // init circuit breaker instance
        JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
                config().getJsonObject("circuit-breaker") : new JsonObject();
        circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbOptions.getInteger("max-failures", 5))
                        .setTimeout(cbOptions.getLong("timeout", 20000L))
                        .setFallbackOnFailure(true)
                        .setResetTimeout(cbOptions.getLong("reset-timeout", 30000L))
        );
    }

    public void publishHttpEndpoint(String name, String host, int port, Handler<AsyncResult<Void>>
            completionHandler) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/",
                new JsonObject().put("api.name", config().getString("api.name", ""))
        );
        publish(record, completionHandler);
    }

    public void publishMessageSource(String name, String address, Class<?> contentClass, Handler<AsyncResult<Void>>
            completionHandler) {
        Record record = MessageSource.createRecord(name, address, contentClass);
        publish(record, completionHandler);
    }

    public void publishMessageSource(String name, String address, Handler<AsyncResult<Void>>
            completionHandler) {
        Record record = MessageSource.createRecord(name, address);
        publish(record, completionHandler);
    }

    public void publishEventBusService(String name, String address, Class<?> serviceClass, Handler<AsyncResult<Void>>
            completionHandler) {
        Record record = EventBusService.createRecord(name, address, serviceClass);
        publish(record, completionHandler);
    }

    protected void publish(Record record, Handler<AsyncResult<Void>> completionHandler) {
        if (discovery == null) {
            try {
                start();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create discovery service");
            }
        }

        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                registeredRecords.add(record);
            }
            completionHandler.handle(ar.map((Void) null));
        });
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        List<Future> futures = new ArrayList<>();
        for (Record record : registeredRecords) {
            Future<Void> unregistrationFuture = Future.future();
            futures.add(unregistrationFuture);
            discovery.unpublish(record.getRegistration(), unregistrationFuture);
        }

        if (futures.isEmpty()) {
            discovery.close();
            future.complete();
        } else {
            CompositeFuture composite = CompositeFuture.all(futures);
            composite.setHandler(ar -> {
                discovery.close();
                if (ar.failed()) {
                    future.fail(ar.cause());
                } else {
                    future.complete();
                }
            });
        }
    }

    protected void publishApiGateway(String host, int port, Handler<AsyncResult<Void>>
            completionHandler) {
        Record record = HttpEndpoint.createRecord("api-gateway", true, host, port, "/", null)
                .setType("api-gateway");
        publish(record, completionHandler);
    }

    public void handleFailure(Logger logger, AsyncResult handler) {
        logger.error(handler.cause().getMessage());
        Future.failedFuture(handler.cause());
    }

    protected void getResponse(HttpMethod method, String path, JsonObject payload, Handler<AsyncResult<Buffer>> handler) {
        int initialOffset = 5; // length of `/api/`
        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> {
            getAllEndpoints().setHandler(ar -> {
                if (ar.succeeded()) {
                    List<Record> recordList = ar.result();
                    if (path.length() <= initialOffset) {
                        handler.handle(Future.failedFuture("Not Found."));
                        return;
                    }
                    String prefix = (path.substring(initialOffset)
                            .split("/"))[0];
                    logger.info("Prefix: " + prefix);
                    // generate new relative path
                    String newPath = path.substring(initialOffset + prefix.length());
                    // get one relevant HTTP client, may not exist
                    logger.info("New path: " + newPath);
                    Optional<Record> client = recordList.stream()
                            .filter(record -> record.getMetadata().getString("api.name") != null)
                            .filter(record -> record.getMetadata().getString("api.name").equals(prefix))
                            .findAny(); // simple load balance

                    if (client.isPresent()) {
                        logger.info("Found client for uri: " + path);
                        doDispatch(newPath, method, payload, discovery.getReference(client.get()).get(), handler, future);
                    } else {
                        logger.info("Client endpoint not found for uri: " + path);
                        handler.handle(Future.failedFuture("Not Found."));
                        future.complete();
                    }
                } else {
                    future.fail(ar.cause());
                }
            });
        }).setHandler(ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    /**
     * Dispatch the request to the downstream REST layers.
     */
    private void doDispatch(String path, HttpMethod method, JsonObject payload, HttpClient client, Handler<AsyncResult<Buffer>> handler, Future<Object> cbFuture) {
        HttpClientRequest toReq = client.request(method, path, response -> {
            response.bodyHandler(body -> {
                if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                    handler.handle(Future.failedFuture(response.toString()));
                    logger.info("Failed to dispatch: " + response.toString());
                } else {
                    handler.handle(Future.succeededFuture(body));
                    logger.info("Successfully dispatched: " + body);
                }
                client.close();
                cbFuture.complete();
                ServiceDiscovery.releaseServiceObject(discovery, client);
            });
        });
        toReq.setChunked(true);
        toReq.putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        if (payload == null) {
            toReq.end();
        } else {
            toReq.write(payload.encode()).end();
        }
    }

    private Future<List<Record>> getAllEndpoints() {
        Future<List<Record>> future = Future.future();
        discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE),
                future.completer());
        return future;
    }
}
