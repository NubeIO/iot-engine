package com.nubeiot.core.common;

import com.nubeiot.core.micro.IMicroProvider;
import com.nubeiot.core.micro.Microservice;
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
@Deprecated
public abstract class RxMicroServiceVerticle extends AbstractVerticle implements IMicroProvider {

    @Getter
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;
    private Microservice microservice;
    protected JsonObject appConfig;

    @Override
    public void start() {
        this.appConfig = Configs.getApplicationCfg(config());
        this.microservice = IMicroProvider.initConfig(vertx, config());
        this.microservice.start();
        this.discovery = this.microservice.getDiscovery();
        this.circuitBreaker = this.microservice.getCircuitBreaker();
    }

    @Override
    public void stop(Future<Void> future) {
        this.microservice.stop(future);
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
        return this.microservice.publish(record);
    }

    protected final Single<Record> publishMessageSource(String name, String address) {
        return this.microservice.publish(MessageSource.createRecord(name, address));
    }

}
