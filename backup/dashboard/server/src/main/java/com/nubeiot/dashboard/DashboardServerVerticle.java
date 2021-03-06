package com.nubeiot.dashboard;

import static com.nubeiot.dashboard.ShareableMongoClient.SHARABLE_MONGO_CLIENT_DATA_KEY;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import io.vertx.reactivex.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.http.HttpConfig;
import com.nubeiot.core.http.HttpConfig.FileStorageConfig.DownloadConfig;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.rest.provider.RestDownloadConfigProvider;
import com.nubeiot.core.http.rest.provider.RestHttpConfigProvider;
import com.nubeiot.core.http.rest.provider.RestMicroContextProvider;
import com.nubeiot.core.http.rest.provider.RestNubeConfigProvider;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.dashboard.controllers.AuthRestController;
import com.nubeiot.dashboard.controllers.GlobalSettingsController;
import com.nubeiot.dashboard.controllers.InfoRestController;
import com.nubeiot.dashboard.controllers.LayoutGridController;
import com.nubeiot.dashboard.controllers.MediaController;
import com.nubeiot.dashboard.controllers.MenuController;
import com.nubeiot.dashboard.controllers.MultiTenantCompanyController;
import com.nubeiot.dashboard.controllers.MultiTenantSiteController;
import com.nubeiot.dashboard.controllers.MultiTenantUserController;
import com.nubeiot.dashboard.controllers.MultiTenantUserGroupController;
import com.nubeiot.dashboard.controllers.QueryHiveController;
import com.nubeiot.dashboard.controllers.QueryPostgreSqlController;
import com.nubeiot.dashboard.controllers.SettingsController;
import com.nubeiot.dashboard.controllers.WidgetImageController;
import com.nubeiot.dashboard.providers.RestOAuth2AuthProvider;
import com.zandero.rest.RestRouter;

public class DashboardServerVerticle extends ContainerVerticle {

    private MicroContext microContext;
    private MongoClient mongoClient;
    private OAuth2Auth oAuth2Auth;

    @Override
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerApi(InfoRestController.class, AuthRestController.class,
                                                                     LayoutGridController.class, MenuController.class,
                                                                     SettingsController.class,
                                                                     GlobalSettingsController.class,
                                                                     WidgetImageController.class, MediaController.class,
                                                                     MultiTenantUserController.class,
                                                                     MultiTenantCompanyController.class,
                                                                     MultiTenantSiteController.class,
                                                                     MultiTenantUserGroupController.class,
                                                                     QueryHiveController.class,
                                                                     QueryPostgreSqlController.class);
        this.addProvider(new HttpServerProvider(router))
            .addProvider(new MicroserviceProvider(), c -> this.microContext = (MicroContext) c);

        this.registerSuccessHandler(event -> {
            this.microContext.rescanService(vertx.eventBus().getDelegate());
            RestRouter.addProvider(RestMicroContextProvider.class, ctx -> new RestMicroContextProvider(microContext));
        });

        JsonObject appConfig = this.nubeConfig.getAppConfig().toJson();
        HttpConfig httpConfig = IConfig.from(appConfig, HttpConfig.class);
        DownloadConfig downloadConfig = IConfig.from(httpConfig, DownloadConfig.class);

        logger.info("Registering Schema: {}", httpConfig.getPublicScheme());
        mongoClient = MongoClient.createNonShared(vertx, appConfig.getJsonObject("mongo"));
        oAuth2Auth = KeycloakAuth.create(vertx, OAuth2FlowType.PASSWORD, appConfig.getJsonObject("keycloak"));

        RestRouter.addProvider(RestNubeConfigProvider.class, ctx -> new RestNubeConfigProvider(this.nubeConfig));
        RestRouter.addProvider(RestOAuth2AuthProvider.class, ctx -> new RestOAuth2AuthProvider(oAuth2Auth));
        RestRouter.addProvider(RestMongoClientProvider.class, ctx -> new RestMongoClientProvider(mongoClient));
        RestRouter.addProvider(RestDownloadConfigProvider.class, ctx -> new RestDownloadConfigProvider(downloadConfig));
        RestRouter.addProvider(RestHttpConfigProvider.class, ctx -> new RestHttpConfigProvider(httpConfig));

        SharedDataDelegate.addLocalDataValue(vertx.getDelegate(), getSharedKey(), SHARABLE_MONGO_CLIENT_DATA_KEY,
                                             new ShareableMongoClient(mongoClient));
    }

}
