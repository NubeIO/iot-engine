package io.nubespark.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.utils.*;
import io.nubespark.vertx.common.RxRestAPIVerticle;
import io.reactivex.Single;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.oauth2.AccessToken;
import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import io.vertx.reactivex.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.Record;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.nubespark.constants.Address.*;
import static io.nubespark.constants.Location.MEDIA_FILE_LOCATION;
import static io.nubespark.constants.Location.WEB_SERVER_MICRO_SERVICE_LOCATION;
import static io.nubespark.constants.Port.HTTP_WEB_SERVER_PORT;
import static io.nubespark.utils.FileUtils.appendRealFileNameWithExtension;
import static io.nubespark.utils.response.ResponseUtils.*;
import static io.nubespark.vertx.common.HttpHelper.failAuthentication;
import static io.nubespark.vertx.common.HttpHelper.serviceUnavailable;

/**
 * Created by topsykretts on 5/4/18.
 */
public class HttpServerVerticle<T> extends RxRestAPIVerticle {
    private OAuth2Auth loginAuth;
    private Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);
    private EventBus eventBus;

    @Override
    protected Logger getLogger() {
        return logger;
    }

    public String getRootFolder() {
        return System.getProperty("user.dir") + WEB_SERVER_MICRO_SERVICE_LOCATION;
    }

    @Override
    public void start(io.vertx.core.Future<Void> future) {
        super.start();
        eventBus = getVertx().eventBus();

        // Register codec for custom message
        eventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());

        logger.info("Config on HttpWebServer is:");
        logger.info(Json.encodePrettily(config()));
        startWebApp()
            .flatMap(httpServer -> publishHttp())
            .flatMap(ignored -> Single.create(source -> getVertx().deployVerticle(MultiTenantVerticle.class.getName(), deployResult -> {
                // Deploy succeed
                if (deployResult.succeeded()) {
                    source.onSuccess("Deployment of MultiTenantVerticle is successful.");
                    logger.info("Deployment of MultiTenantVerticle is successful.");
                } else {
                    // Deploy failed
                    source.onError(deployResult.cause());
                    deployResult.cause().printStackTrace();
                }
            })))
            .flatMap(ignored -> Single.create(source -> getVertx().deployVerticle(DynamicSiteCollectionHandleVerticle.class.getName(), deployResult -> {
                // Deploy succeed
                if (deployResult.succeeded()) {
                    source.onSuccess("Deployment of DynamicSiteCollectionHandleVerticle is successful.");
                    logger.info("Deployment of DynamicSiteCollectionHandleVerticle is successful.");
                } else {
                    // Deploy failed
                    source.onError(deployResult.cause());
                    deployResult.cause().printStackTrace();
                }
            })))
            .subscribe(ignored -> future.complete(), future::fail);
    }

    private Single<HttpServer> startWebApp() {
        loginAuth = KeycloakAuth.create(vertx, OAuth2FlowType.PASSWORD, config().getJsonObject("keycloak"));

        // Create a router object.
        Router router = Router.router(vertx);

        // creating body handler
        router.route().handler(BodyHandler.create().setUploadsDirectory(getRootFolder() + MEDIA_FILE_LOCATION).setBodyLimit(5000000)); // limited to 5 MB
        // handle the form

        enableCorsSupport(router);
        handleAuth(router);
        handleAPIs(router);
        handleAuthEventBus(router);
        handleEventBus(router);
        handleGateway(router);
        handleStaticResource(router);

        // Create the HTTP server and pass the "accept" method to the request handler.
        return createHttpServer(router, config().getString("http.host", "0.0.0.0"), config().getInteger("http.port", HTTP_WEB_SERVER_PORT))
            .doOnSuccess(httpServer -> logger.info("Web Server started at " + httpServer.actualPort()))
            .doOnError(throwable -> logger.error("Cannot start server: " + throwable.getLocalizedMessage()));
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint(SERVICE_NAME, "0.0.0.0", config().getInteger("http.port", 8085))
            .doOnSubscribe(res -> logger.info("Publish successful HttpWebServer."))
            .doOnError(throwable -> logger.error("Cannot publish HttpWebServer: " + throwable.getLocalizedMessage()));
    }

    private void handleGateway(Router router) {
        // api dispatcher
        router.route("/api/*").handler(this::dispatchRequests);
    }

    /**
     * An implementation of handling authentication system and response on the authentic URLs only
     *
     * @param router for routing the URLs
     */
    private void handleAuth(Router router) {
        router.route("/api/login/account").handler(this::handleLogin);
        router.route("/api/refreshToken").handler(this::refreshAccessToken);
        router.route("/api/*").handler(this::authMiddleWare);
        router.route("/api/currentUser").handler(this::currentUser);
        router.route("/api/logout").handler(this::redirectLogout);
    }


    private void handleAPIs(Router router) {
        router.route("/api/*").handler(this::handleMultiTenantSupportAPIs);
        router.route("/api/layout_grid/*").handler(ctx -> this.handleDynamicSiteCollection(ctx, "layout_grid"));
        router.route("/api/menu/*").handler(ctx -> this.handleDynamicSiteCollection(ctx, "menu"));
        router.route("/api/settings/*").handler(ctx -> this.handleDynamicSiteCollection(ctx, "settings"));
        router.post("/api/upload_image").handler(this::handleUploadImage);
    }

    private void handleMultiTenantSupportAPIs(RoutingContext ctx) {
        String url = ctx.normalisedPath().substring("/api/".length());
        JsonObject header = new JsonObject()
            .put("url", url)
            .put("method", ctx.request().method())
            .put("user", ctx.user().principal())
            .put("host", ctx.request().host())
            .put("keycloakConfig", config().getJsonObject("keycloak"));

        T body;
        if (StringUtils.isNull(ctx.getBody().toString())) {
            body = (T) new JsonObject();
        } else if (SQLUtils.in(url.split("/")[0], "delete_users", "delete_companies", "delete_sites", "delete_user_groups")) {
            body = (T) ctx.getBodyAsJsonArray();
            logger.info("Body:::::::::" + body);
        } else {
            body = (T) ctx.getBodyAsJson();
        }
        logger.info("Body:::::" + body);
        CustomMessage<T> message = new CustomMessage<>(header, body, 200);
        eventBus.send(MULTI_TENANT_ADDRESS, message, reply -> {
            if (reply.succeeded()) {
                CustomMessage replyMessage = (CustomMessage) reply.result().body();
                logger.info("Received reply: " + replyMessage.getBody());
                if (replyMessage.getStatusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                    ctx.next();
                } else {
                    ctx.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(replyMessage.getStatusCode())
                        .end(replyMessage.getBody().toString());
                }
            } else {
                ctx.response().setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end();
                logger.info("No reply from cluster receiver");
            }
        });
    }

    private void handleUploadImage(RoutingContext ctx) {
        if (ctx.fileUploads().size() > 0) {
            FileUpload fileUpload = ctx.fileUploads().iterator().next();
            ctx.response()
                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .setStatusCode(HttpResponseStatus.CREATED.code())
                .end(Json.encodePrettily(new JsonObject().put("path", appendRealFileNameWithExtension(fileUpload).replace(getRootFolder(), ""))));
        } else {
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
        }
    }

    private void handleDynamicSiteCollection(RoutingContext ctx, String collection) {
        JsonObject header = new JsonObject()
            .put("url", ctx.normalisedPath().substring(("/api/" + collection + "/").length()))
            .put("method", ctx.request().method())
            .put("user", ctx.user().principal())
            .put("collection", collection);
        JsonObject body;
        if (StringUtils.isNull(ctx.getBody().toString())) {
            body = new JsonObject();
        } else {
            body = ctx.getBodyAsJson();
        }
        CustomMessage<JsonObject> message = new CustomMessage<>(header, body, 200);
        eventBus.send(DYNAMIC_SITE_COLLECTION_ADDRESS, message, reply -> {
            if (reply.succeeded()) {
                CustomMessage replyMessage = (CustomMessage) reply.result().body();
                logger.info("Received reply: " + replyMessage.getBody());
                ctx.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(replyMessage.getStatusCode())
                    .end(replyMessage.getBody().toString());
            } else {
                ctx.response().setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end();
                logger.info("No reply from cluster receiver");
            }
        });
    }

    private void handleAuthEventBus(Router router) {
        router.route("/eventbus/*").handler((RoutingContext ctx) -> {
            String authorization = ctx.request().getDelegate().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Basic")) {
                handleBasicAuth(ctx, authorization);
            } else {
                setAuthenticUser(ctx, ctx.request().getParam("access_token"));
            }
        });
    }

    private void handleBasicAuth(RoutingContext ctx, String authorization) {
        if (authorization != null && authorization.startsWith("Basic")) {
            authorization = authorization.substring("Basic ".length());
            byte decodedAuthorization[] = Base64.getDecoder().decode(authorization);
            String basicAuthString = new String(decodedAuthorization, StandardCharsets.UTF_8);
            String username = basicAuthString.split(":")[0];
            String password = basicAuthString.split(":")[1];
            loginAuth.rxGetToken(new JsonObject().put("username", username).put("password", password))
                .subscribe(token -> ctx.next(), throwable -> failAuthentication(ctx));
        }
    }

    private void handleEventBus(Router router) {
        BridgeOptions options = new BridgeOptions()
            .addOutboundPermitted(new PermittedOptions().setAddress("news-feed"))
            .addOutboundPermitted(new PermittedOptions().setAddress("io.nubespark.ditto.events"));

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options, event -> {
            // You can also optionally provide a handler like this which will be passed any events that occur on the bridge
            // You can use this for monitoring or logging, or to change the raw messages in-flight.
            // It can also be used for fine grained access control.
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                logger.info("A socket was created");
            }
            // This signals that it's ok to process the event
            event.complete(true);
        }));
    }

    /**
     * By default index.html from webroot/ is available on route "/" only.
     * <p>
     * For single page application, when we did refresh the page then we firstly need to return index.html then the
     * requested APIs values. So here we are making the index.html page available for those actions.
     *
     * @param router routing the URLs
     */
    private void handleStaticResource(Router router) {
        router.route().handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(getRootFolder()));
        router.route("/*").handler(ctx -> ctx.response().sendFile(getRootFolder() + "/index.html"));
    }

    private void setAuthenticUser(RoutingContext ctx, String authorization) {
        loginAuth.introspectToken(authorization, res -> {
            if (res.succeeded()) {
                System.out.println("Auth Success");
                AccessToken token = res.result();

                String user_id = token.principal().getString("sub");
                String access_token = token.principal().getString("access_token");
                dispatchRequests(HttpMethod.GET, URL.get_user + "/" + user_id, null)
                    .subscribe(buffer -> {
                        io.vertx.ext.auth.User user = new UserImpl(new JsonObject()
                            .put("access_token", access_token).mergeIn(buffer.toJsonObject()));

                        ctx.setUser(new User(user));
                        ctx.next();
                    }, throwable -> serviceUnavailable(ctx, throwable));
            } else {
                System.out.println("Auth Fail");
                res.cause().printStackTrace();
                failAuthentication(ctx);
            }
        });
    }

    private void redirectLogout(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        User user = ctx.user();
        String access_token = user.principal().getString("access_token");
        String refresh_token = body.getString("refresh_token");
        JsonObject keycloakConfig = config().getJsonObject("keycloak");
        String client_id = keycloakConfig.getString("resource");
        String client_secret = keycloakConfig
            .getJsonObject("credentials").getString("secret");
        String realmName = keycloakConfig.getString("realm");
        String uri = keycloakConfig.getString("auth-server-url")
            + "/realms/" + realmName + "/protocol/openid-connect/logout";

        HttpClient client = vertx.createHttpClient();

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            ctx.response().setStatusCode(response.statusCode()).end();
        });
        request.setChunked(true);

        String body$ = "refresh_token=" + refresh_token + "&client_id=" + client_id
            + "&client_secret=" + client_secret;
        request.putHeader("content-type", "application/x-www-form-urlencoded");
        request.putHeader("Authorization", "Bearer " + access_token);

        request.write(body$).end();
    }

    private void handleLogin(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String username = body.getString("username");
        String password = body.getString("password");

        loginAuth.rxGetToken(new JsonObject().put("username", username).put("password", password))
            .subscribe(token -> {
                ctx.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .end(Json.encodePrettily(token.principal()));
            }, throwable -> failAuthentication(ctx));
    }

    private void authMiddleWare(RoutingContext ctx) {
        String authorization = ctx.request().getDelegate().getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null) {
            authorization = authorization.substring("Bearer ".length());
            System.out.println(authorization);
            setAuthenticUser(ctx, authorization);
        } else {
            failAuthentication(ctx);
        }
    }

    private void currentUser(RoutingContext ctx) {
        User user = ctx.user();
        if (user != null) {
            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + ctx.user().principal().getString("group_id"), null)
                .flatMap(group -> {
                    JsonObject object = group.toJsonObject();
                    return dispatchRequests(HttpMethod.GET, URL.get_site + "/" + group.toJsonObject().getString("site_id"), null)
                        .map(site -> object.put("site", site.toJsonObject()
                            .put("logo_sm", buildAbsoluteUri(ctx, site.toJsonObject().getString("logo_sm")))
                            .put("logo_md", buildAbsoluteUri(ctx, site.toJsonObject().getString("logo_md")))));
                })
                .subscribe(userGroup -> {
                    ctx.response().putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encodePrettily(user.principal()
                            .put("group", userGroup)));
                });
        } else {
            logger.info("Send not authorized error and user should login");
            failAuthentication(ctx);
        }
    }

    private void refreshAccessToken(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String refresh_token = body.getString("refresh_token");
        String access_token = ctx.request().getHeader("Authorization"); // Bearer {{token}}
        JsonObject keycloakConfig = config().getJsonObject("keycloak");
        String client_id = keycloakConfig.getString("resource");
        String client_secret = keycloakConfig
            .getJsonObject("credentials").getString("secret");
        String realmName = keycloakConfig.getString("realm");
        String uri = keycloakConfig.getString("auth-server-url")
            + "/realms/" + realmName + "/protocol/openid-connect/token";
        HttpClient client = vertx.createHttpClient();

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            response.bodyHandler(body$ -> {
                if (response.statusCode() != 200) {
                    ctx.response().setStatusCode(response.statusCode()).end();
                } else {
                    HttpServerResponse toRsp = ctx.response()
                        .setStatusCode(response.statusCode());
                    response.headers().getDelegate().forEach(header -> {
                        toRsp.putHeader(header.getKey(), header.getValue());
                    });
                    // send response
                    toRsp.end(body$);
                }
            });
        });
        request.setChunked(true);

        String body$ = "refresh_token=" + refresh_token + "&client_id=" + client_id
            + "&client_secret=" + client_secret + "&grant_type=refresh_token";
        request.putHeader("content-type", "application/x-www-form-urlencoded");
        request.putHeader("Authorization", access_token);

        request.write(body$).end();
    }
}
