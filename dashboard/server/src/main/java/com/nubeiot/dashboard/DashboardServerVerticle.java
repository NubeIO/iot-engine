package com.nubeiot.dashboard;

import static com.nubeiot.core.http.base.HttpScheme.HTTPS;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpConfig;
import com.nubeiot.core.http.HttpServerContext;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.RegisterScheme;
import com.nubeiot.core.http.RestConfigProvider;
import com.nubeiot.core.http.ServerInfo;
import com.nubeiot.core.http.base.HttpScheme;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.dashboard.controllers.AuthRestController;
import com.nubeiot.dashboard.controllers.InfoRestController;
import com.nubeiot.dashboard.controllers.LayoutGridController;
import com.nubeiot.dashboard.controllers.MenuController;
import com.nubeiot.dashboard.providers.RestMediaDirProvider;
import com.nubeiot.dashboard.providers.RestOAuth2AuthProvider;
import com.zandero.rest.RestRouter;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import io.vertx.reactivex.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.servicediscovery.types.HttpLocation;

@SuppressWarnings("Duplicates")
public class DashboardServerVerticle extends ContainerVerticle {

    private HttpServerContext httpContext;
    private MicroContext microContext;
    private MongoClient mongoClient;
    private String mediaDir;
    private OAuth2Auth oAuth2Auth;

    @Override
    @SuppressWarnings("Duplicates")
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerApi(InfoRestController.class, AuthRestController.class,
                                                                     LayoutGridController.class, MenuController.class);
        this.addProvider(new HttpServerProvider(router), c -> this.httpContext = (HttpServerContext) c)
            .addProvider(new MicroserviceProvider(), c -> this.microContext = (MicroContext) c);

        this.registerSuccessHandler(event -> {
            ServerInfo info = this.httpContext.getServerInfo();
            microContext.getClusterController()
                        .addHttpRecord("DashboardServerVerticle",
                                       new HttpLocation(info.toJson()).setRoot(info.getApiPath()), new JsonObject())
                        .subscribe();
        });

        JsonObject appConfig = this.nubeConfig.getAppConfig().toJson();
        DashboardServerConfig dashboardServerConfig = IConfig.from(appConfig, DashboardServerConfig.class);
        HttpConfig httpConfig = IConfig.from(appConfig, HttpConfig.class);

        logger.info("Registering Schema: {}", httpConfig.getScheme());
        registerHttpScheme(HttpScheme.valueOf(httpConfig.getScheme()));
        mongoClient = MongoClient.createNonShared(vertx, appConfig.getJsonObject("mongo"));
        mediaDir = FileUtils.createFolder(nubeConfig.getDataDir().toString(), dashboardServerConfig.getMediaPath());
        oAuth2Auth = KeycloakAuth.create(vertx, OAuth2FlowType.PASSWORD, appConfig.getJsonObject("keycloak"));

        RestRouter.addProvider(RestConfigProvider.class,
                               ctx -> new RestConfigProvider(config(), this.nubeConfig.getAppConfig().toJson()));
        RestRouter.addProvider(RestOAuth2AuthProvider.class, ctx -> new RestOAuth2AuthProvider(oAuth2Auth));
        RestRouter.addProvider(RestMongoClientProvider.class, ctx -> new RestMongoClientProvider(mongoClient));
        RestRouter.addProvider(RestMediaDirProvider.class, ctx -> new RestMediaDirProvider(mediaDir));
    }

    private void registerHttpScheme(HttpScheme scheme) {
        if (scheme == HTTPS) {
            new RegisterScheme().register(HttpScheme.HTTPS);
        } else {
            new RegisterScheme().register(HttpScheme.HTTP);
        }
    }

}
