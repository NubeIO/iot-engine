package com.nubeiot.dashboard.impl;

import static com.nubeiot.core.common.utils.response.ResponseUtils.CONTENT_TYPE;
import static com.nubeiot.core.common.utils.response.ResponseUtils.CONTENT_TYPE_JSON;
import static com.nubeiot.core.http.HttpScheme.HTTPS;
import static com.nubeiot.dashboard.constants.Address.DYNAMIC_SITE_COLLECTION_ADDRESS;
import static com.nubeiot.dashboard.constants.Address.MULTI_TENANT_ADDRESS;
import static com.nubeiot.dashboard.constants.Address.SERVICE_NAME;
import static com.nubeiot.dashboard.constants.Address.SITE_COLLECTION_ADDRESS;
import static com.nubeiot.dashboard.constants.Collection.COMPANY;
import static com.nubeiot.dashboard.constants.Collection.MEDIA_FILES;
import static com.nubeiot.dashboard.constants.Collection.MENU;
import static com.nubeiot.dashboard.constants.Collection.SETTINGS;
import static com.nubeiot.dashboard.constants.Collection.SITE;
import static com.nubeiot.dashboard.constants.Collection.USER;
import static com.nubeiot.dashboard.constants.Collection.USER_GROUP;
import static com.nubeiot.dashboard.utils.FileUtils.appendRealFileNameWithExtension;
import static com.nubeiot.dashboard.utils.MongoUtils.idQuery;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.oauth2.AccessToken;
import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import io.vertx.reactivex.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.common.HttpHelper;
import com.nubeiot.core.common.RxRestAPIVerticle;
import com.nubeiot.core.common.constants.Port;
import com.nubeiot.core.common.utils.CustomMessage;
import com.nubeiot.core.common.utils.CustomMessageCodec;
import com.nubeiot.core.common.utils.SQLUtils;
import com.nubeiot.core.http.HttpScheme;
import com.nubeiot.core.http.RegisterScheme;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.utils.MongoUtils;
import com.nubeiot.dashboard.utils.ResourceUtils;

/**
 * Created by topsykretts on 5/4/18.
 */
public class HttpServerVerticle extends RxRestAPIVerticle {

    private OAuth2Auth loginAuth;
    private EventBus eventBus;
    private MongoClient mongoClient;
    private String workingDir = "";
    private String mediaRoot = "";

    @Override
    protected Single<String> onStartComplete() {
        registerHttpScheme();
        mongoClient = MongoClient.createNonShared(vertx, appConfig.getJsonObject("mongo").getJsonObject("config"));
        eventBus = getVertx().eventBus();

        // Register codec for custom message
        eventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
        workingDir = FileUtils.createFolder(appConfig.getString("DATA_DIR"));
        mediaRoot = appConfig.getString("MEDIA_ROOT");

        logger.info("Config on HttpWebServer is:");
        logger.info(Json.encodePrettily(config()));
        return startWebApp().flatMap(httpServer -> publishHttp())
                            .flatMap(ignored -> Single.create(
                                source -> getVertx().deployVerticle(MultiTenantVerticle.class.getName(),
                                                                    new DeploymentOptions().setConfig(config()),
                                                                    deployResult -> {
                                                                        // Deploy succeed
                                                                        if (deployResult.succeeded()) {
                                                                            source.onSuccess(
                                                                                "Deployment of MultiTenantVerticle is" +
                                                                                " successful.");
                                                                            logger.info(
                                                                                "Deployment of MultiTenantVerticle is" +
                                                                                " successful.");
                                                                        } else {
                                                                            // Deploy failed
                                                                            source.onError(deployResult.cause());
                                                                            deployResult.cause().printStackTrace();
                                                                        }
                                                                    })))
                            .flatMap(ignored -> Single.create(
                                source -> getVertx().deployVerticle(DynamicSiteCollectionHandleVerticle.class.getName(),
                                                                    new DeploymentOptions().setConfig(config()),
                                                                    deployResult -> {
                                                                        // Deploy succeed
                                                                        if (deployResult.succeeded()) {
                                                                            source.onSuccess("Deployment of " +
                                                                                             "DynamicSiteCollectionHandleVerticle " +
                                                                                             "is successful.");
                                                                            logger.info("Deployment of " +
                                                                                        "DynamicSiteCollectionHandleVerticle " +
                                                                                        "is successful.");
                                                                        } else {
                                                                            // Deploy failed
                                                                            source.onError(deployResult.cause());
                                                                            deployResult.cause().printStackTrace();
                                                                        }
                                                                    })))
                            .flatMap(ignored -> Single.create(
                                source -> getVertx().deployVerticle(SiteCollectionHandleVerticle.class.getName(),
                                                                    new DeploymentOptions().setConfig(config()),
                                                                    deployResult -> {
                                                                        // Deploy succeed
                                                                        if (deployResult.succeeded()) {
                                                                            source.onSuccess("Deployment of " +
                                                                                             "SiteCollectionHandleVerticle is " +
                                                                                             "successful.");
                                                                            logger.info("Deployment of " +
                                                                                        "SiteCollectionHandleVerticle" +
                                                                                        " is " + "successful.");
                                                                        } else {
                                                                            // Deploy failed
                                                                            source.onError(deployResult.cause());
                                                                            deployResult.cause().printStackTrace();
                                                                        }
                                                                    })));
    }

    private void registerHttpScheme() {
        String schema = appConfig.getString("http.scheme");
        if (schema.equals(HTTPS.toString())) {
            new RegisterScheme().register(HttpScheme.HTTPS);
        } else {
            new RegisterScheme().register(HttpScheme.HTTP);
        }
    }

    private Single<HttpServer> startWebApp() {
        loginAuth = KeycloakAuth.create(vertx, OAuth2FlowType.PASSWORD, appConfig.getJsonObject("keycloak"));

        // Create a router object.
        Router router = Router.router(vertx);

        // creating body handler
        FileUtils.createFolder(workingDir, mediaRoot);
        router.route()
              .handler(BodyHandler.create()
                                  .setUploadsDirectory(Urls.combinePath(workingDir, mediaRoot))
                                  .setBodyLimit(5000000)); // limited to 5 MB
        // handle the form

        enableCorsSupport(router);
        handleAuth(router);
        handleAPIs(router);
        handleAuthEventBus(router);
        handleEventBus(router);
        handleGateway(router);
        handleStaticResource(router);

        // Create the HTTP server and pass the "accept" method to the request handler.
        return createHttpServer(router, appConfig.getString("http.host", "0.0.0.0"),
                                appConfig.getInteger("http.port", Port.HTTP_WEB_SERVER_PORT)).doOnSuccess(
            httpServer -> logger.info("Web Server started at " + httpServer.actualPort()))
                                                                                             .doOnError(
                                                                                                 throwable -> logger.error(
                                                                                                     "Cannot start " +
                                                                                                     "server: " +
                                                                                                     throwable.getLocalizedMessage()));
    }

    private void handleStaticResource(Router router) {
        router.route()
              .handler(StaticHandler.create()
                                    .setCachingEnabled(false)
                                    .setAllowRootFileSystemAccess(true)
                                    .setWebRoot(workingDir));
    }

    private Single<Record> publishHttp() {
        return publishHttpEndpoint(SERVICE_NAME, "0.0.0.0", appConfig.getInteger("http.port", 8085)).doOnSubscribe(
            res -> logger.info("Publish successful HttpWebServer."))
                                                                                                    .doOnError(
                                                                                                        throwable -> logger
                                                                                                                         .error(
                                                                                                                             "Cannot publish HttpWebServer: " +
                                                                                                                             throwable
                                                                                                                                 .getLocalizedMessage()));
    }

    private void handleGateway(Router router) {
        // api dispatcher
        router.route("/api/*").handler(ctx -> {
            String[] values = ctx.normalisedPath().split("/");
            String gatewayAPIPrefix = "";
            if (values.length > 2) {
                gatewayAPIPrefix = "/" + values[1] + "/" + values[2] + ".*";
            }
            logger.info("Query: " +
                        new JsonObject().put("gatewayAPIPrefix", new JsonObject().put("$regex", gatewayAPIPrefix))
                                        .put("site_id", ctx.request().headers().getDelegate().get("Site-Id")));
            mongoClient.rxFindOne(SETTINGS, new JsonObject().put("gatewayAPIPrefix",
                                                                 new JsonObject().put("$regex", gatewayAPIPrefix))
                                                            .put("site_id",
                                                                 ctx.request().headers().getDelegate().get("Site-Id")),
                                  null).subscribe(settings -> {
                if (settings != null) {
                    this.dispatchRequests(ctx, settings);
                } else {
                    this.dispatchRequests(ctx, new JsonObject());
                }
            });
        });
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
        router.route("/api/widget_image/*").handler(ctx -> this.handleDynamicSiteCollection(ctx, "widget_image"));
        router.route("/api/query_pg/*").handler(ctx -> this.handleSiteCollection(ctx, "query_pg"));
        router.route("/api/query_hive/*").handler(ctx -> this.handleSiteCollection(ctx, "query_hive"));
        router.post("/api/media_file").handler(this::handlePostMediaFile);
        router.get("/api/media_file/:id").handler(this::handleGetMediaFile);
        router.get("/api/menu_for_user_group/*").handler(this::handleMenuForUserGroup);
    }

    private void handleMultiTenantSupportAPIs(RoutingContext ctx) {
        String url = ctx.normalisedPath().substring("/api/".length());
        JsonObject header = new JsonObject().put("url", url)
                                            .put("method", ctx.request().method())
                                            .put(HttpHeaders.AUTHORIZATION.toString(),
                                                 ctx.request().headers().get(HttpHeaders.AUTHORIZATION.toString()))
                                            .put("user", ctx.user().principal())
                                            .put("host", ctx.request().host())
                                            .put("Site-Id", ctx.request().headers().get("Site-Id"))
                                            .put("keycloakConfig", appConfig.getJsonObject("keycloak"));

        Object body;
        if (Strings.isBlank(ctx.getBody().toString())) {
            body = new JsonObject();
        } else if (SQLUtils.in(url.split("/")[0], "delete_users", "delete_companies", "delete_sites",
                               "delete_user_groups")) {
            body = ctx.getBodyAsJsonArray();
        } else {
            try {
                body = ctx.getBodyAsJson();
            } catch (Exception ex) {
                ctx.next();
                return;
            }
        }
        CustomMessage<Object> message = new CustomMessage<>(header, body, 200);
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

    private void handlePostMediaFile(RoutingContext ctx) {
        if (!ctx.fileUploads().isEmpty()) {
            JsonObject output = new JsonObject();
            Observable.fromIterable(ctx.fileUploads()).flatMapSingle(fileUpload -> {
                String name = appendRealFileNameWithExtension(fileUpload).replace(
                    Urls.combinePath(workingDir, mediaRoot) + "/", "");
                return mongoClient.rxInsert(MEDIA_FILES,
                                            new JsonObject().put("name", name).put("title", fileUpload.name()))
                                  .map(id -> output.put(fileUpload.name(), id));
            }).toList().subscribe(ignored -> {
                ctx.response()
                   .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                   .setStatusCode(HttpResponseStatus.CREATED.code())
                   .end(Json.encodePrettily(output));
            }, e -> ctx.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(e.getMessage()));
        } else {
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
    }

    private void handleGetMediaFile(RoutingContext ctx) {
        String id = ctx.request().getParam("id");
        mongoClient.rxFindOne(MEDIA_FILES, MongoUtils.idQuery(id), null)
                   .map(mediaRecord -> {
                       JsonObject record = new JsonObject();
                       if (mediaRecord != null) {
                           record.put("absolute_path", ResourceUtils.buildAbsolutePath(ctx.request().host(), mediaRoot,
                                                                                       mediaRecord.getString("name")));
                       }
                       return record;
                   })
                   .subscribe(record -> ctx.response()
                                           .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                                           .setStatusCode(HttpResponseStatus.OK.code())
                                           .end(record.encode()));
    }

    private void handleMenuForUserGroup(RoutingContext ctx) {
        if (SQLUtils.in(ctx.user().principal().getString("role"), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            String siteId = ctx.normalisedPath().substring(("/api/menu_for_user_group/").length());
            mongoClient.rxFindOne(MENU, new JsonObject().put("site_id", siteId), null).subscribe(menu -> {
                if (menu != null) {
                    ctx.response()
                       .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                       .setStatusCode(HttpResponseStatus.OK.code())
                       .end(menu.toString());
                } else {
                    ctx.response()
                       .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                       .setStatusCode(HttpResponseStatus.OK.code())
                       .end(new JsonObject().toString());
                }
            });
        } else {
            ctx.response().setStatusCode(HttpResponseStatus.FORBIDDEN.code()).end();
        }
    }

    private void handleDynamicSiteCollection(RoutingContext ctx, String collection) {
        handleCollectionAPIs(ctx, collection, DYNAMIC_SITE_COLLECTION_ADDRESS);
    }

    private void handleSiteCollection(RoutingContext ctx, String collection) {
        handleCollectionAPIs(ctx, collection, SITE_COLLECTION_ADDRESS);
    }

    private void handleCollectionAPIs(RoutingContext ctx, String collection, String address) {
        JsonObject header = new JsonObject().put("url", ctx.normalisedPath()
                                                           .substring(("/api/" + collection).length())
                                                           .replaceAll("^/", ""))
                                            .put("method", ctx.request().method())
                                            .put("user", ctx.user().principal())
                                            .put("Site-Id", ctx.request().headers().get("Site-Id"))
                                            .put("host", ctx.request().host())
                                            .put("collection", collection);
        JsonObject body;
        if (Strings.isBlank(ctx.getBody().toString())) {
            body = new JsonObject();
        } else {
            body = ctx.getBodyAsJson();
        }
        CustomMessage<JsonObject> message = new CustomMessage<>(header, body, 200);
        // noinspection Duplicates
        eventBus.send(address, message, reply -> {
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
            byte[] decodedAuthorization = Base64.getDecoder().decode(authorization);
            String basicAuthString = new String(decodedAuthorization, StandardCharsets.UTF_8);
            String username = basicAuthString.split(":")[0];
            String password = basicAuthString.split(":")[1];
            loginAuth.rxGetToken(new JsonObject().put("username", username).put("password", password))
                     .subscribe(token -> ctx.next(), throwable -> HttpHelper.failAuthentication(ctx));
        }
    }

    private void handleEventBus(Router router) {
        BridgeOptions options = new BridgeOptions().addOutboundPermitted(
            new PermittedOptions().setAddress("io.nubespark.ditto.events"));

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options, event -> {
            // You can also optionally provide a handler like this which will be passed any events that occur on the
            // bridge
            // You can use this for monitoring or logging, or to change the raw messages in-flight.
            // It can also be used for fine grained access control.
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                logger.info("A socket was created");
            }
            // This signals that it's ok to process the event
            event.complete(true);
        }));
    }

    private void setAuthenticUser(RoutingContext ctx, String authorization) {
        loginAuth.introspectToken(authorization, res -> {
            if (res.succeeded()) {
                System.out.println("Auth Success");
                AccessToken token = res.result();

                String username = token.principal().getString("username");
                String access_token = token.principal().getString("access_token");
                mongoClient.rxFindOne(USER, new JsonObject().put("username", username), null).subscribe(response -> {
                    io.vertx.ext.auth.User user = new UserImpl(
                        new JsonObject().put("access_token", access_token).mergeIn(response));

                    ctx.setUser(new User(user));
                    ctx.next();
                }, throwable -> HttpHelper.serviceUnavailable(ctx, throwable));
            } else {
                System.out.println("Auth Fail");
                res.cause().printStackTrace();
                HttpHelper.failAuthentication(ctx);
            }
        });
    }

    private void redirectLogout(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        User user = ctx.user();
        String access_token = user.principal().getString("access_token");
        String refresh_token = body.getString("refresh_token");
        JsonObject keycloakConfig = appConfig.getJsonObject("keycloak");
        String client_id = keycloakConfig.getString("resource");
        String client_secret = keycloakConfig.getJsonObject("credentials").getString("secret");
        String realmName = keycloakConfig.getString("realm");
        String uri = keycloakConfig.getString("auth-server-url") + "/realms/" + realmName +
                     "/protocol/openid-connect/logout";

        HttpClient client = vertx.createHttpClient();

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            ctx.response().setStatusCode(response.statusCode()).end();
        });
        request.setChunked(true);

        String body$ = "refresh_token=" + refresh_token + "&client_id=" + client_id + "&client_secret=" + client_secret;
        request.putHeader("content-type", "application/x-www-form-urlencoded");
        request.putHeader("Authorization", "Bearer " + access_token);

        request.write(body$).end();
    }

    private void handleLogin(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String username = body.getString("username");
        String password = body.getString("password");

        loginAuth.rxGetToken(new JsonObject().put("username", username).put("password", password)).subscribe(token -> {
            ctx.response().putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encodePrettily(token.principal()));
        }, throwable -> HttpHelper.failAuthentication(ctx));
    }

    private void authMiddleWare(RoutingContext ctx) {
        logger.info("Auth middleware is being called...");
        String authorization = ctx.request().getDelegate().getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null) {
            authorization = authorization.substring("Bearer ".length());
            System.out.println(authorization);
            setAuthenticUser(ctx, authorization);
        } else {
            // Web pages WebSocket authentication
            String[] contents = ctx.request().getDelegate().getHeader("X-Original-URI").split("access_token=");
            if (contents.length == 2) {
                logger.info("Params Access token: " + contents[1]);
                authorization = contents[1].substring("Bearer%20".length());
                logger.info("Access Token: " + authorization);
                setAuthenticUser(ctx, authorization);
            } else {
                String[] credentials = ctx.request()
                                          .getDelegate()
                                          .getHeader("X-Original-URI")
                                          .replaceFirst("/ws/[^?]*(\\?)?", "")
                                          .split(":::");
                // NodeRED WebSocket authentication
                if (credentials.length == 2) {
                    loginAuth.rxGetToken(
                        new JsonObject().put("username", credentials[0]).put("password", credentials[1]))
                             .subscribe(token -> {
                                 ctx.response()
                                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                                    .putHeader("username", credentials[0])
                                    .end(Json.encodePrettily(token.principal()));
                             }, throwable -> HttpHelper.failAuthentication(ctx));
                } else {
                    HttpHelper.failAuthentication(ctx);
                }
            }
        }
    }

    private void currentUser(RoutingContext ctx) {
        User user = ctx.user();
        String groupId = ctx.user().principal().getString("group_id");
        String siteId = ctx.user().principal().getString("site_id");
        JsonArray sitesIds = user.principal().getJsonArray("sites_ids", new JsonArray());
        Single.just(new JsonObject()).flatMap(object -> {
            if (Strings.isNotBlank(groupId)) {
                return mongoClient.rxFindOne(USER_GROUP, idQuery(groupId), null).map(group -> {
                    if (group != null) {
                        return object.put("group", group);
                    }
                    return object;
                });
            } else {
                return Single.just(object);
            }
        }).flatMap(group -> {
            if (Strings.isNotBlank(siteId)) {
                return mongoClient.rxFindOne(SITE, idQuery(siteId), null).flatMap(site -> {
                    if (site != null) {
                        return Single.just(group.put("site", site));
                    } else {
                        return assignSiteOnAvailability(ctx, group);
                    }
                });
            } else {
                return assignSiteOnAvailability(ctx, group);
            }
        }).flatMap(groupAndSite -> {
            if (sitesIds.size() > 0) {
                return mongoClient.rxFind(SITE, new JsonObject().put("_id", new JsonObject().put("$in", sitesIds)))
                                  .map(respondSites -> groupAndSite.put("sites", respondSites));
            } else {
                return Single.just(groupAndSite);
            }
        }).flatMap(groupAndSite -> {
            String associatedCompanyId = user.principal().getString("company_id", "");
            if (Strings.isNotBlank(associatedCompanyId)) {
                return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null).map(response -> {
                    if (response != null) {
                        return groupAndSite.put("company", response);
                    }
                    return groupAndSite;
                });
            } else {
                return Single.just(groupAndSite);
            }
        }).subscribe(groupAndSiteAndCompany -> {
            ctx.response()
               .putHeader("username", user.principal().getString("username")) // Use case: ditto NGINX
               .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
               .end(Json.encodePrettily(user.principal().mergeIn(groupAndSiteAndCompany)));
        });
    }

    private SingleSource<? extends JsonObject> assignSiteOnAvailability(RoutingContext ctx, JsonObject group) {
        String role = ctx.user().principal().getString("role");
        if (SQLUtils.in(role, Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            // If we have already a site for its respective role, then we will assign it
            JsonObject query = new JsonObject().put("associated_company_id",
                                                    ctx.user().principal().getString("company_id"));
            return mongoClient.rxFind(SITE, query).flatMap(sites -> {
                if (sites.size() > 0) {
                    JsonObject site$ = sites.get(0);
                    String siteId$ = sites.get(0).getString("_id");
                    JsonObject update$ = new JsonObject().put("$set", new JsonObject().put("site_id", siteId$));
                    return mongoClient.rxUpdateCollectionWithOptions(USER, query, update$,
                                                                     new UpdateOptions(false, true))
                                      .map(absSite -> group.put("site",
                                                                site$.put("site", site$).put("site_id", siteId$)));
                } else {
                    return Single.just(group);
                }
            });
        }
        return Single.just(group);
    }

    private void refreshAccessToken(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String refresh_token = body.getString("refresh_token");
        String access_token = ctx.request().getHeader("Authorization"); // Bearer {{token}}
        JsonObject keycloakConfig = appConfig.getJsonObject("keycloak");
        String client_id = keycloakConfig.getString("resource");
        String client_secret = keycloakConfig.getJsonObject("credentials").getString("secret");
        String realmName = keycloakConfig.getString("realm");
        String uri = keycloakConfig.getString("auth-server-url") + "/realms/" + realmName +
                     "/protocol/openid-connect/token";
        HttpClient client = vertx.createHttpClient();

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            response.bodyHandler(body$ -> {
                if (response.statusCode() != 200) {
                    ctx.response().setStatusCode(response.statusCode()).end();
                } else {
                    HttpServerResponse toRsp = ctx.response().setStatusCode(response.statusCode());
                    response.headers().getDelegate().forEach(header -> {
                        toRsp.putHeader(header.getKey(), header.getValue());
                    });
                    // send response
                    toRsp.end(body$);
                }
            });
        });
        request.setChunked(true);

        String body$ = "refresh_token=" + refresh_token + "&client_id=" + client_id + "&client_secret=" +
                       client_secret + "&grant_type=refresh_token";
        request.putHeader("content-type", "application/x-www-form-urlencoded");
        request.putHeader("Authorization", access_token);

        request.write(body$).end();
    }

}
