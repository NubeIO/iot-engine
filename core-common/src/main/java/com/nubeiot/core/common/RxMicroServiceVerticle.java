package com.nubeiot.core.common;

import com.nubeiot.core.micro.IMicroVerticle;
import com.nubeiot.core.micro.MicroserviceConfig;
import com.nubeiot.core.utils.Configs;

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
    private MicroserviceConfig microserviceConfig;
    protected JsonObject appConfig;

    @Override
    public void start() {
        this.appConfig = Configs.getApplicationCfg(config());
        this.microserviceConfig = IMicroVerticle.initConfig(vertx, config()).onStart();
        this.discovery = this.microserviceConfig.getDiscovery();
        this.circuitBreaker = this.microserviceConfig.getCircuitBreaker();
    }

    @Override
    public void stop(Future<Void> future) {
        this.microserviceConfig.onStop(future);
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

    //TODO: CHECK CONFIG FOR OTHER MAVEN MODULE
    protected final Single<Record> publishHttpEndpoint(String name, String host, int port) {
        String apiName = this.appConfig.getString("api.name", "");
        Record record = HttpEndpoint.createRecord(name, host, port, "/", new JsonObject().put("api.name", apiName));
        return this.microserviceConfig.publish(record);
    }

    protected final Single<Record> publishMessageSource(String name, String address) {
        return this.microserviceConfig.publish(MessageSource.createRecord(name, address));
    }

}
