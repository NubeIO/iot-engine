package io.nubespark.vertx.common;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link Verticle} taking care of the discovery and publication of services.
 *
 */
public class MicroServiceVerticle extends AbstractVerticle {

  protected ServiceDiscovery discovery;
  protected CircuitBreaker circuitBreaker;
  protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

  @Override
  public void start() {

      //initializing service discovery
      discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

      // init circuit breaker instance
      JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
              config().getJsonObject("circuit-breaker") : new JsonObject();
      circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
              new CircuitBreakerOptions()
                      .setMaxFailures(cbOptions.getInteger("max-failures", 5))
                      .setTimeout(cbOptions.getLong("timeout", 10000L))
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
      completionHandler.handle(ar.map((Void)null));
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
}
