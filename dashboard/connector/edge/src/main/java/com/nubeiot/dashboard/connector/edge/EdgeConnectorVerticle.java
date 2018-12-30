package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.IMicroProvider;
import com.nubeiot.core.sql.ISqlProvider;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;

public final class EdgeConnectorVerticle extends AbstractVerticle
        implements ISqlProvider, IMicroProvider, HttpServerProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private NubeConfig nubeConfig;
    private IComponent sqlWrapper;
    private IComponent microService;
    private IComponent httpServer;

    @Override
    public void start() throws Exception {
        this.nubeConfig = IConfig.from(config(), NubeConfig.class);
        this.httpServer = HttpServerProvider.create(this.vertx, nubeConfig, initHttpRouter());
        //        this.sqlWrapper = ISqlProvider.create(this.vertx, this.nubeConfig, () -> Single.just(new JsonObject
        //        ()));
        logger.info("Dashboard Edge connector configuration: {}", this.nubeConfig.getAppConfig().toJson());
        this.httpServer.start();
        //        this.sqlWrapper.start();
        super.start();
    }

    @SuppressWarnings("unchecked")
    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerApi(EdgeRestController.class).registerEventBusApi(EdgeRestEventApi.class);
    }

    @Override
    public void stop() throws Exception {
        //        this.sqlWrapper.stop();
        this.httpServer.stop();
        super.stop();
    }

}
