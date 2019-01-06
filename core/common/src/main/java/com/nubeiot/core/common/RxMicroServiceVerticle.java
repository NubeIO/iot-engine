package com.nubeiot.core.common;

import io.reactivex.Single;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.reactivex.servicediscovery.types.MessageSource;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.Microservice;
import com.nubeiot.core.micro.MicroserviceProvider;

import lombok.Getter;

/**
 * An implementation of {@link Verticle} taking care of the discovery and publication of services.
 */
@Deprecated
public abstract class RxMicroServiceVerticle extends ContainerVerticle {

    @Getter
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Microservice microservice;
    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;
    protected JsonObject appConfig;

    @Override
    public void start() {
        super.start();
        this.addProvider(new MicroserviceProvider(), microservice -> {
            this.microservice = microservice;
            this.discovery = this.microservice.getDiscovery();
            this.circuitBreaker = this.microservice.getCircuitBreaker();
            this.appConfig = this.nubeConfig.getAppConfig().toJson();
        });
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
        String apiName = this.appConfig.getString("api.name", "");
        Record record = HttpEndpoint.createRecord(name, host, port, "/", new JsonObject().put("api.name", apiName));
        return this.microservice.publish(record);
    }

    protected final Single<Record> publishMessageSource(String name, String address) {
        return this.microservice.publish(MessageSource.createRecord(name, address));
    }

}
