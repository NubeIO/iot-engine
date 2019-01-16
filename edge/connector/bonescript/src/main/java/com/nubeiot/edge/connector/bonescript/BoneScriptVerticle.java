package com.nubeiot.edge.connector.bonescript;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.sql.SQLWrapper;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.connector.bonescript.handlers.PointsEventHandler;
import com.nubeiot.edge.connector.bonescript.model.DefaultCatalog;
import com.nubeiot.edge.connector.bonescript.operations.BoneScript;
import com.nubeiot.edge.connector.bonescript.operations.Ditto;
import com.nubeiot.edge.connector.bonescript.operations.Historian;
import com.nubeiot.edge.connector.bonescript.utils.DittoDBUtils;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.Getter;

public class BoneScriptVerticle extends ContainerVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String BB_DEFAULT_VERSION = "v15";
    public static final String BB_VERSION = "bb_version";

    @Getter
    private BoneScriptEntityHandler entityHandler;
    @Getter
    private EventController eventController;
    @Getter
    MultiThreadDittoDB multiThreadDittoDB;

    @Override
    public void start() {
        super.start();

        final NubeConfig nubeConfig = IConfig.from(config(), NubeConfig.class);
        logger.info("BoneScript configuration: {}", this.nubeConfig.getAppConfig().toJson());

        System.getProperties()
              .setProperty(BB_VERSION, nubeConfig.getAppConfig().toJson().getString(BB_VERSION, BB_DEFAULT_VERSION));

        this.addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, BoneScriptEntityHandler.class),
                         this::handler);
        this.addProvider(new HttpServerProvider(initHttpRouter()), httpServer -> {});

        this.eventController = registerEventBus(new EventController(vertx));
    }

    private void handler(SQLWrapper component) {
        this.entityHandler = (BoneScriptEntityHandler) component.getEntityHandler();

        DittoDBUtils.getDittoData(this.entityHandler).subscribe(db -> {
            multiThreadDittoDB = new MultiThreadDittoDB(entityHandler);
            // Initializing Ditto for future use
            Ditto.init(db);
            logger.info("Ditto Initialization is successfully done.");
            Historian.init(vertx, this.entityHandler, multiThreadDittoDB, db);
            BoneScript.init(vertx, this.entityHandler, multiThreadDittoDB, db);
        });
    }

    @SuppressWarnings("unchecked")
    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerApi(BoneScriptRestController.class)
                                     .registerEventBusApi(BoneScriptRestEventApi.class);
    }

    private EventController registerEventBus(EventController controller) {
        controller.consume(BoneScriptEventBus.POINTS, new PointsEventHandler(this, BoneScriptEventBus.POINTS));
        return controller;
    }

}
