package com.nubeiot.core.common;

import io.reactivex.Single;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.servicediscovery.types.MessageSource;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;

import lombok.Getter;

/**
 * An implementation of {@link Verticle} taking care of the discovery and publication of services.
 */
@Deprecated
public abstract class RxMicroServiceVerticle extends ContainerVerticle {

    @Getter
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected JsonObject appConfig;
    protected MicroContext microContext;

    @Override
    public void start() {
        super.start();
        this.appConfig = this.nubeConfig.getAppConfig().toJson();
        this.addProvider(new MicroserviceProvider(), this::setMicroContext);
    }

    protected abstract Single<String> onStartComplete();

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
        return microContext.getClusterController().addHttpRecord(name, new HttpLocation().setHost(host).setPort(port).setRoot("/"),
                                                 new JsonObject().put("api.name", apiName));
    }

    protected final Single<Record> publishMessageSource(String name, String address) {
        return microContext.getClusterController().addRecord(MessageSource.createRecord(name, address));
    }

    private void setMicroContext(MicroContext context) {
        this.microContext = context;
        this.onStartComplete()
            .subscribe(logger::info, error -> logger.error("Caused issue due to: {}", error.getCause()));
    }

}
