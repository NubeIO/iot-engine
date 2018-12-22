package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.IMicroProvider;
import com.nubeiot.core.sql.ISqlProvider;
import com.nubeiot.core.utils.Configs;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import lombok.Getter;

public final class EdgeConnectorVerticle extends AbstractVerticle
        implements ISqlProvider, IMicroProvider, HttpServerProvider {

    @Getter
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private JsonObject appConfig;
    private IComponent sqlWrapper;
    private IComponent microService;
    private IComponent httpServer;

    @Override
    public void start() throws Exception {
        this.appConfig = Configs.getApplicationCfg(config());
        logger.info("Config on app store REST {}", this.appConfig);
        this.httpServer = HttpServerProvider.create(this.vertx, config(), initHttpRouter());
        this.sqlWrapper = ISqlProvider.create(this.vertx, config(), () -> Single.just(new JsonObject()));
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
