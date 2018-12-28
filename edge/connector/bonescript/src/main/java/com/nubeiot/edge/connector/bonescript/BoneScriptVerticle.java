package com.nubeiot.edge.connector.bonescript;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.sql.SQLWrapper;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.connector.bonescript.jwt.JwtHandler;
import com.nubeiot.edge.connector.bonescript.model.DefaultCatalog;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.Getter;

public class BoneScriptVerticle extends ContainerVerticle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String BB_DEFAULT_VERSION = "v15";
    public static final String BB_VERSION = "bb_version";

    @Getter
    private BoneScriptEntityHandler boneScriptEntityHandler;

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
    }

    private void handler(SQLWrapper component) {
        this.boneScriptEntityHandler = (BoneScriptEntityHandler) component.getEntityHandler();
    }

    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerApi(JwtHandler.class, BoneScriptRestController.class);
    }

}
