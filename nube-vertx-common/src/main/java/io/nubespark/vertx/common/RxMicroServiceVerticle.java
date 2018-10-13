package io.nubespark.vertx.common;

import com.nubeio.iot.share.MicroserviceConfig;
import com.nubeio.iot.share.IMicroVerticle;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.reactivex.servicediscovery.types.MessageSource;
import io.vertx.servicediscovery.Record;
import lombok.Getter;

/**
 * An implementation of {@link Verticle} taking care of the discovery and publication of services.
 */
public abstract class RxMicroServiceVerticle extends AbstractVerticle implements IMicroVerticle {

    @Getter
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;
    @Getter
    protected MicroserviceConfig microserviceConfig;

    public void start() {
        this.microserviceConfig = IMicroVerticle.initConfig(vertx, config()).onStart();
        this.discovery = this.microserviceConfig.getDiscovery();
        this.circuitBreaker = this.microserviceConfig.getCircuitBreaker();
    }

    /**
     * Create http server for the REST service.
     *
     * @param router router instance
     * @param host   http host
     * @param port   http port
     * @return async result of the procedure
     */
    protected Single<HttpServer> createHttpServer(Router router, String host, int port) {
        return vertx.createHttpServer().requestHandler(router::accept).rxListen(port, host);
    }

    protected final Single<Record> publishHttpEndpoint(String name, String host, int port) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/",
                                                  new JsonObject().put("api.name", config().getString("api.name", "")));
        return this.microserviceConfig.publish(record);
    }

    protected final Single<Record> publishMessageSource(String name, String address) {
        return this.microserviceConfig.publish(MessageSource.createRecord(name, address));
    }

    @Override
    public void stop(Future<Void> future) {
        this.microserviceConfig.onStop(future);
    }

}
