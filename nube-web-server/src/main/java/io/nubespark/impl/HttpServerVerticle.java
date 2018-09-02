package io.nubespark.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.Role;
import io.nubespark.controller.HttpException;
import io.nubespark.impl.models.*;
import io.nubespark.utils.SQLUtils;
import io.nubespark.utils.StringUtils;
import io.nubespark.utils.URL;
import io.nubespark.utils.UserUtils;
import io.nubespark.vertx.common.HttpHelper;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.oauth2.AccessToken;
import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import io.vertx.reactivex.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.nubespark.utils.Constants.SERVICE_NAME;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;
import static io.nubespark.vertx.common.HttpHelper.*;

/**
 * Created by topsykretts on 5/4/18.
 */
public class HttpServerVerticle extends RxMicroServiceVerticle {

    private OAuth2Auth loginAuth;
    private Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void start(io.vertx.core.Future<Void> future) {
        super.start();
        logger.info("Config on HttpWebServer is:");
        logger.info(Json.encodePrettily(config()));
        startWebApp().flatMap(httpServer -> publishHttp())
            .subscribe(ignored -> future.complete(), future::fail);
    }

    private Single<HttpServer> startWebApp() {
        loginAuth = KeycloakAuth.create(vertx, OAuth2FlowType.PASSWORD, config().getJsonObject("keycloak"));

        // Create a router object.
        Router router = Router.router(vertx);

        // creating body handler
        router.route().handler(BodyHandler.create());

        enableCorsSupport(router);
        handleAuth(router);
        handleMultiTenantSupportAPIs(router);
        handleAuthEventBus(router);
        handleEventBus(router);
        handleGateway(router);
        handleStaticResource(router);

        // Create the HTTP server and pass the "accept" method to the request handler.
        return vertx.createHttpServer()
            .requestHandler(router::accept)
            .rxListen(
                // Retrieve the port from the configuration,
                // default to 8085.
                config().getInteger("http.port", 8085)
            )
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

    private void handleMultiTenantSupportAPIs(Router router) {
        router.post("/api/user").handler(this::handlePostUser);
        router.post("/api/company").handler(this::handlePostCompany);
        router.post("/api/site").handler(this::handlePostSite);
        router.post("/api/user_group").handler(this::handlePostUserGroup);

        router.get("/api/companies").handler(this::handleGetCompanies);
        router.get("/api/users").handler(this::handleGetUsers);
        router.get("/api/sites").handler(this::handleGetSites);
        router.get("/api/user_groups").handler(this::handleGetUserGroups);

        router.post("/api/delete_users").handler(this::handleDeleteUsers);
        router.post("/api/delete_companies").handler(this::handleDeleteCompanies);
        router.post("/api/delete_sites").handler(this::handleDeleteSites);
        router.post("/api/delete_user_groups").handler(this::handleDeleteUserGroups);

        router.post("/api/check_user").handler(this::handleCheckUser);
        router.patch("/api/password/:id").handler(this::handleUpdatePassword);
        router.patch("/api/user/:id").handler(this::handleUpdateUser);
        router.patch("/api/site/:id").handler(this::handleUpdateSite);
        router.patch("/api/user_group/:id").handler(this::handleUpdateUserGroup);
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
                System.out.println("A socket was created");
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
        router.route().handler(StaticHandler.create());
        router.route("/*").handler(ctx -> ctx.response().sendFile("webroot/index.html"));
    }

    private void setAuthenticUser(RoutingContext ctx, String authorization) {
        loginAuth.introspectToken(authorization, res -> {
            if (res.succeeded()) {
                System.out.println("Auth Success");
                AccessToken token = res.result();

                String user_id = token.principal().getString("sub");
                String access_token = token.principal().getString("access_token");
                logger.info("User id: " + user_id);
                dispatchRequests(HttpMethod.GET, URL.get_user + "/" + user_id, null)
                    .subscribe(buffer -> {
                        logger.info("User Response: " + buffer.toJsonObject());
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
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            if (role == Role.SUPER_ADMIN) {
                ctx.response().putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .end(Json.encodePrettily(user.principal()));
            } else {
                dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + ctx.user().principal().getString("group_id"), null)
                    .flatMap(group -> {
                        JsonObject object = group.toJsonObject();
                        return dispatchRequests(HttpMethod.GET, URL.get_site + "/" + group.toJsonObject().getString("site_id"), null)
                            .map(site -> object.put("site", site.toJsonObject()));
                    })
                    .subscribe(userGroup -> {
                        ctx.response().putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(user.principal()
                                .put("group", userGroup)));
                    });
            }
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


    //region Multi-Tenant handlers -------------------------------------------------------------------------------------
    private void handlePostUser(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonObject body = ctx.getBodyAsJson();
            JsonObject user = ctx.user().principal();
            KeycloakUserRepresentation userRepresentation = new KeycloakUserRepresentation(body);
            String accessToken = user.getString("access_token");
            JsonObject keycloakConfig = config().getJsonObject("keycloak");
            HttpClient client = vertx.createHttpClient();

            String authServerUrl = keycloakConfig.getString("auth-server-url");
            String realmName = keycloakConfig.getString("realm");

            // 1. Create User on Keycloak
            UserUtils.createUser(userRepresentation, accessToken, keycloakConfig.getString("auth-server-url"), keycloakConfig.getString("realm"), client)
                // 2. GET recently created user details from Keycloak
                .flatMap(ignored -> UserUtils.getUserFromUsername(body.getString("username"), accessToken, authServerUrl, realmName, client))
                // 3. Resetting password; by default password: 'helloworld'
                .flatMap(keycloakUser -> UserUtils.resetPassword(keycloakUser.getString("id"), body.getString("password", "helloworld"), accessToken, authServerUrl, realmName, client)
                    .flatMap(ignored -> {
                        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                            // 4.1 only child companies can make associate with it's users
                            return dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject().put("associated_company_id", user.getString("company_id")))
                                .flatMap(response -> {
                                    JsonArray childCompanies = new JsonArray(response.getDelegate());
                                    if (childCompanies.size() > 0) {
                                        // 5.1 Proceed for creating MongoDB user
                                        return createMongoUser(body, user, accessToken, client, authServerUrl, realmName, keycloakUser, childCompanies);
                                    } else {
                                        // 5.2 Remove user from Keycloak
                                        return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName, client)
                                            .map(ign -> {
                                                throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <Company> at first.");
                                            });
                                    }
                                });
                        } else {
                            // 4.2 Creating user on MongoDB with 'group_id'
                            return createMongoUser(body, user, accessToken, client, authServerUrl, realmName, keycloakUser, null);
                        }
                    })).subscribe(statusCode -> ctx.response().setStatusCode(statusCode).end(), throwable -> handleHttpException(throwable, ctx));

        }
    }

    private SingleSource<? extends Integer> createMongoUser(JsonObject body, JsonObject user, String accessToken, HttpClient client, String authServerUrl,
                                                            String realmName, JsonObject keycloakUser, JsonArray childCompanies) {
        return dispatchRequests(HttpMethod.POST, URL.get_user_group, new JsonObject().put("associated_company_id", user.getString("company_id")))
            .flatMap(response -> {
                JsonArray childGroups = new JsonArray(response.getDelegate());
                if (childGroups.size() > 0) {
                    // 5.1 Creating user on MongoDB
                    if (childCompanies != null) {
                        String[] _ids = StringUtils.getIds(childCompanies);
                        body.put("company_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("company_id", ""), _ids));
                    } else {
                        body.put("company_id", user.getString("company_id"));
                    }

                    String[] _ids = StringUtils.getIds(childGroups);
                    body.put("associated_company_id", user.getString("company_id"))
                        .put("group_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("group_id", ""), _ids));
                    MongoUser mongoUser = new MongoUser(body, user, keycloakUser);
                    logger.info("Mongo User::: " + mongoUser.toJsonObject());
                    return dispatchRequests(HttpMethod.POST, URL.post_user, mongoUser.toJsonObject())
                        .map(buffer -> HttpResponseStatus.CREATED.code());
                } else {
                    // 5.2 Remove user from Keycloak
                    return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName, client)
                        .map(ign -> {
                            throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <User Group> at first.");
                        });
                }
            });
    }


    private SingleSource<? extends Integer> updateMongoUser(JsonObject ctxUser, JsonObject body, JsonObject keycloakUser, JsonArray childGroups) {
        return dispatchRequests(HttpMethod.POST, URL.get_user_group, new JsonObject().put("associated_company_id", ctxUser.getString("company_id")))
            .flatMap(response -> {
                JsonArray childCompanies = new JsonArray(response.getDelegate());
                if (childCompanies.size() > 0) {
                    {
                        String[] _ids = StringUtils.getIds(childGroups);
                        body.put("company_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("company_id", ""), _ids));
                    }
                    String[] _ids = StringUtils.getIds(childGroups);
                    body.put("associated_company_id", ctxUser.getString("company_id"))
                        .put("group_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("group_id", ""), _ids));
                    MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
                    logger.info("Mongo User::: " + mongoUser.toJsonObject());
                    return dispatchRequests(HttpMethod.PUT, URL.put_user, mongoUser.toJsonObject())
                        .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
                } else {
                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <User Group> at first.");
                }
            });
    }

    private SingleSource<? extends Integer> updateOwnUser(JsonObject body, JsonObject ctxUser, JsonObject keycloakUser) {
        // User doesn't have the authority to update own company_id, associated_company_id, and group_id
        body.put("company_id", ctxUser.getString("company_id"))
            .put("associated_company_id", ctxUser.getString("associated_company_id"))
            .put("group_id", ctxUser.getString("group_id"));
        MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
        JsonObject mongoUserObject = mongoUser.toJsonObject().put("role", ctxUser.getString("role")); // Role should be overriden
        logger.info("Mongo User::: " + mongoUser.toJsonObject());
        return dispatchRequests(HttpMethod.PUT, URL.put_user, mongoUserObject)
            .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
    }

    private void handlePostCompany(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            Company company = new Company(ctx.getBodyAsJson(), ctx.user().principal());
            dispatchRequests(HttpMethod.POST, URL.post_company, company.toJsonObject())
                .subscribe(
                    result -> ctx.response().setStatusCode(new JsonObject(result.getDelegate()).getInteger("statusCode")).end(),
                    throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private void handlePostSite(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            Site site = new Site(ctx.getBodyAsJson()
                .put("associated_company_id", ctx.user().principal().getString("company_id"))
                .put("role", UserUtils.getRole(role).toString()));
            dispatchRequests(HttpMethod.POST, URL.post_site, site.toJsonObject())
                .subscribe(
                    siteResponse -> ctx.response().setStatusCode(new JsonObject(siteResponse.getDelegate()).getInteger("statusCode")).end(),
                    throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private void handlePostUserGroup(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            // Only manager's sites should make available for user_group
            dispatchRequests(HttpMethod.POST, URL.get_site, new JsonObject().put("associated_company_id", ctx.user().principal().getString("company_id")))
                .map(buffer -> {
                    JsonArray childCompaniesResponse = new JsonArray(buffer.getDelegate());
                    if (childCompaniesResponse.size() > 0) {
                        String[] availableSites = StringUtils.getIds(childCompaniesResponse);
                        String site_id = SQLUtils.getMatchValueOrDefaultOne(ctx.getBodyAsJson().getString("site_id", ""), availableSites);
                        return new UserGroup(ctx.getBodyAsJson()
                            .put("associated_company_id", ctx.user().principal().getString("company_id"))
                            .put("role", UserUtils.getRole(role).toString())
                            .put("site_id", site_id));
                    } else {
                        throw badRequest("Create <Site> at first.");
                    }
                })
                .flatMap(userGroup -> dispatchRequests(HttpMethod.POST, URL.post_user_group, userGroup.toJsonObject()))
                .subscribe(
                    buffer -> ctx.response().setStatusCode(new JsonObject(buffer.getDelegate()).getInteger("statusCode")).end(),
                    throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private void handleGetCompanies(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithAssociateCompanyRepresentation(ctx, new JsonObject().put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))), URL.get_company);
        } else if (role == Role.ADMIN) {
            respondRequestWithAssociateCompanyRepresentation(ctx, new JsonObject().put("associated_company_id", ctx.user().principal().getString("company_id")), URL.get_company);
        } else {
            forbidden(ctx);
        }
    }

    private void handleGetUsers(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithCompanyAssociateCompanyAndGroupRepresentation(ctx, new JsonObject().put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))), URL.get_user);
        } else if (role == Role.ADMIN) {
            // Returning all <Users> which is branches from the ADMIN
            dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject()
                .put("associated_company_id", ctx.user().principal().getString("company_id"))
                .put("role", Role.MANAGER.toString()))
                .subscribe(buffer -> respondRequestWithCompanyAssociateCompanyAndGroupRepresentation(ctx, new JsonObject()
                        .put("associated_company_id", new JsonObject()
                            .put("$in", StringUtils.getIdsJsonArray(new JsonArray(buffer.getDelegate()))
                                .add(ctx.user().principal().getString("company_id")))), URL.get_user),
                    throwable -> handleHttpException(throwable, ctx));
        } else if (role == Role.MANAGER) {
            respondRequestWithCompanyAssociateCompanyAndGroupRepresentation(ctx, new JsonObject().put("associated_company_id", ctx.user().principal().getString("company_id")), URL.get_user);
        } else {
            forbidden(ctx);
        }
    }

    private void handleGetSites(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithAssociateCompanyRepresentation(ctx, new JsonObject(), URL.get_site);
        } else if (role == Role.ADMIN) {
            // Returning all MANAGER's companies' <sites> which is associated with the ADMIN company
            dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject()
                .put("associated_company_id", ctx.user().principal().getString("company_id"))
                .put("role", Role.MANAGER.toString()))
                .subscribe(
                    buffer -> respondRequestWithAssociateCompanyRepresentation(ctx, new JsonObject()
                        .put("associated_company_id", new JsonObject()
                            .put("$in", StringUtils.getIdsJsonArray(new JsonArray(buffer.getDelegate()))
                                .add(ctx.user().principal().getString("company_id")))), URL.get_site),
                    throwable -> handleHttpException(throwable, ctx));
        } else if (role == Role.MANAGER) {
            respondRequestWithAssociateCompanyRepresentation(ctx, new JsonObject().put("associated_company_id", ctx.user().principal().getString("company_id")), URL.get_site);
        } else {
            forbidden(ctx);
        }
    }

    private void handleGetUserGroups(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithSiteAndAssociateCompanyRepresentation(ctx, new JsonObject(), URL.get_user_group);
        } else if (role == Role.ADMIN) {
            // Returning all MANAGER's companies' <user groups> which is associated with the ADMIN company
            dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject()
                .put("associated_company_id", ctx.user().principal().getString("company_id"))
                .put("role", Role.MANAGER.toString()))
                .subscribe(
                    buffer -> respondRequestWithSiteAndAssociateCompanyRepresentation(ctx, new JsonObject()
                        .put("associated_company_id", new JsonObject()
                            .put("$in", StringUtils.getIdsJsonArray(new JsonArray(buffer.getDelegate()))
                                .add(ctx.user().principal().getString("company_id")))), URL.get_user_group),
                    throwable -> handleHttpException(throwable, ctx));
        } else if (role == Role.MANAGER) {
            respondRequestWithSiteAndAssociateCompanyRepresentation(ctx, new JsonObject().put("associated_company_id", ctx.user().principal().getString("company_id")), URL.get_user_group);
        } else {
            forbidden(ctx);
        }
    }

    private void handleDeleteUsers(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        // Model level permission; this is limited to SUPER_ADMIN, ADMIN and MANAGER
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonArray queryInput = ctx.getBodyAsJsonArray();
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));

            dispatchRequests(HttpMethod.POST, URL.get_user, query)
                .map(buffer -> {
                    JsonArray users = new JsonArray(buffer.getDelegate());
                    if (users.size() == queryInput.size()) {
                        String companyId = ctx.user().principal().getString("company_id");
                        for (Object userResponse : users) {
                            JsonObject user = (JsonObject) (userResponse);
                            if (!user.getString("associated_company_id").equals(companyId)) {
                                throw new HttpException(HttpResponseStatus.FORBIDDEN, "You don't have permission to perform the action.");
                            }
                        }
                        return users;
                    }
                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Doesn't have those Users on Database.");
                })
                .flatMap(users -> Observable.fromIterable(users)
                    .flatMapSingle(user -> deleteUserFromKeycloakAndMongo(ctx, user))
                    .toList())
                .subscribe(ignored -> ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(), throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private SingleSource<? extends Integer> deleteUserFromKeycloakAndMongo(RoutingContext ctx, Object userObject) {
        JsonObject user = (JsonObject) (userObject);
        JsonObject keycloakConfig = HttpServerVerticle.this.config().getJsonObject("keycloak");
        HttpClient client = vertx.createHttpClient();

        return UserUtils.deleteUser(user.getString("_id"),
            ctx.user().principal().getString("access_token"),
            keycloakConfig.getString("auth-server-url"),
            keycloakConfig.getString("realm"),
            client)
            .flatMap(deleteUserKeycloakResponse -> {
                if (deleteUserKeycloakResponse.getInteger("statusCode") == HttpResponseStatus.NO_CONTENT.code()) {
                    JsonObject queryToDeleteOne = new JsonObject().put("_id", new JsonObject()
                        .put("$in", new JsonArray().add(user.getString("_id"))));

                    return dispatchRequests(HttpMethod.POST, URL.delete_user, queryToDeleteOne)
                        .map(deleteUserResponse -> {
                            if (StringUtils.isNotNull(deleteUserResponse.toString())) {
                                throw new HttpException(new JsonObject(deleteUserResponse.getDelegate()).getInteger("statusCode"), "Users are unable to deleted from the services.");
                            }
                            return HttpResponseStatus.NO_CONTENT.code();
                        });
                } else {
                    throw new HttpException(deleteUserKeycloakResponse.getInteger("statusCode"), "Users are unable to deleted from the services.");
                }
            });
    }

    private void handleDeleteCompanies(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        // Model level permission; this is limited to SUPER_ADMIN and ADMIN
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            JsonArray queryInput = ctx.getBodyAsJsonArray();
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            dispatchRequests(HttpMethod.POST, URL.get_company, query)
                .flatMap(buffer -> {
                    JsonArray companies = new JsonArray(buffer.getDelegate());
                    if (companies.size() == queryInput.size()) {
                        String companyId = ctx.user().principal().getString("company_id");
                        boolean objectLevelPermission = true;
                        for (Object companyResponse : companies) {
                            JsonObject company = (JsonObject) (companyResponse);
                            if (!company.getString("associated_company_id").equals(companyId)) {
                                objectLevelPermission = false;
                            }
                        }
                        if (objectLevelPermission) {
                            return dispatchRequests(HttpMethod.POST, URL.delete_company, query);
                        } else {
                            throw forbidden();
                        }

                    } else {
                        throw badRequest("Doesn't have those <Companies> on Database.");
                    }
                })
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        throw new HttpException(new JsonObject(buffer.getDelegate()).getInteger("statusCode"));
                    }
                    return HttpResponseStatus.NO_CONTENT.code();
                }).subscribe(ignored -> ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(), throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private void handleDeleteSites(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        // Model level permission
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonArray queryInput = ctx.getBodyAsJsonArray();
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            dispatchRequests(HttpMethod.POST, URL.get_site, query)
                .flatMap(buffer -> {
                    JsonArray sites = new JsonArray(buffer.getDelegate());
                    if (sites.size() == queryInput.size()) {
                        String companyId = ctx.user().principal().getString("company_id");
                        boolean objectLevelPermission = true;
                        for (Object siteResponse : sites) {
                            JsonObject site = (JsonObject) (siteResponse);
                            if (!site.getString("associated_company_id").equals(companyId)) {
                                objectLevelPermission = false;
                            }
                        }
                        if (objectLevelPermission) {
                            return dispatchRequests(HttpMethod.POST, URL.delete_site, query);
                        } else {
                            throw forbidden();
                        }
                    } else {
                        throw badRequest("Doesn't have those <Sites> on Database.");
                    }
                })
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        throw new HttpException(new JsonObject(buffer.getDelegate()).getInteger("statusCode"));
                    }
                    return HttpResponseStatus.NO_CONTENT.code();
                }).subscribe(ignored -> ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(), throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private void handleDeleteUserGroups(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        // Model level permission
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonArray queryInput = ctx.getBodyAsJsonArray();
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            dispatchRequests(HttpMethod.POST, URL.get_user_group, query)
                .flatMap(buffer -> {
                    JsonArray userGroups = new JsonArray(buffer.getDelegate());
                    if (userGroups.size() == queryInput.size()) {
                        String companyId = ctx.user().principal().getString("company_id");
                        boolean objectLevelPermission = true;
                        for (Object userGroupResponse : userGroups) {
                            JsonObject userGroup = (JsonObject) (userGroupResponse);
                            if (!userGroup.getString("associated_company_id").equals(companyId)) {
                                objectLevelPermission = false;
                            }
                        }
                        if (objectLevelPermission) {
                            return dispatchRequests(HttpMethod.POST, URL.delete_user_group, query);
                        } else {
                            throw forbidden();
                        }
                    } else {
                        throw badRequest("Doesn't have those <User Groups> on Database.");
                    }
                })
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        throw new HttpException(new JsonObject(buffer.getDelegate()).getInteger("statusCode"));
                    }
                    return HttpResponseStatus.NO_CONTENT.code();
                }).subscribe(ignored -> ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(), throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private void handleCheckUser(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String username = body.getString("username", "");
        String email = body.getString("email", "");
        String query = "username=" + username + "&email=" + email;

        JsonObject user = ctx.user().principal();
        String accessToken = user.getString("access_token");
        JsonObject keycloakConfig = config().getJsonObject("keycloak");
        HttpClient client = vertx.createHttpClient();

        String authServerUrl = keycloakConfig.getString("auth-server-url");
        String realmName = keycloakConfig.getString("realm");

        UserUtils.queryUsers(query, accessToken, authServerUrl, realmName, client)
            .subscribe(users -> {
                logger.info("Users: " + users);
                int usersSize = users.stream().filter(userObject -> {
                    JsonObject jsonUser = (JsonObject) userObject;
                    if (StringUtils.isNull(username)) {
                        return jsonUser.getString("email").equals(email);
                    } else if (StringUtils.isNull(email)) {
                        return jsonUser.getString("username").equals(username);
                    } else {
                        return jsonUser.getString("username").equals(username) && jsonUser.getString("email").equals(email);
                    }
                }).collect(Collectors.toList()).size();
                logger.info("Size of user match: " + usersSize);
                if (usersSize > 0) {
                    ctx.response().setStatusCode(HttpResponseStatus.FOUND.code()).end();
                } else {
                    ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
                }
            }, throwable -> handleHttpException(throwable, ctx));
    }

    private void handleUpdatePassword(RoutingContext ctx) {
        JsonObject ctxUser = ctx.user().principal();
        String userId = ctx.request().getParam("id");
        String password = ctx.getBodyAsJson().getString("password", "");
        Role role = Role.valueOf(ctxUser.getString("role"));
        String accessToken = ctxUser.getString("access_token");
        JsonObject keycloakConfig = config().getJsonObject("keycloak");
        HttpClient client = vertx.createHttpClient();

        String authServerUrl = keycloakConfig.getString("auth-server-url");
        String realmName = keycloakConfig.getString("realm");

        if (StringUtils.isNotNull(password)) {
            dispatchRequests(HttpMethod.GET, URL.get_user + "/" + userId, null)
                .map(response -> {
                    if (StringUtils.isNull(response.toString())) {
                        throw new HttpException(HttpResponseStatus.BAD_REQUEST);
                    } else {
                        return new JsonObject(response.getDelegate());
                    }
                })
                .flatMap(user -> {
                    // Own password can be changed or those users passwords which is associated with some company
                    if (ctxUser.getString("user_id").equals(userId) ||
                        (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString()) &&
                            ctxUser.getString("company_id").equals(user.getString("associated_company_id")))) {
                        return UserUtils.resetPassword(userId, password, accessToken, authServerUrl, realmName, client);
                    } else {
                        throw forbidden();
                    }
                }).subscribe(ignored -> ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(), throwable -> handleHttpException(throwable, ctx));
        } else {
            ctx.response()
                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                .end(Json.encodePrettily(new JsonObject().put("message", "Password can't be NULL.")));
        }
    }

    private void handleUpdateUser(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        JsonObject ctxUser = ctx.user().principal();
        System.out.println("User principle: " + ctxUser);
        JsonObject body = ctx.getBodyAsJson();
        String userId = ctx.request().getParam("id");
        String accessToken = ctx.user().principal().getString("access_token");
        KeycloakUserRepresentation keycloakUserRepresentation = new KeycloakUserRepresentation(body);
        JsonObject keycloakConfig = config().getJsonObject("keycloak");
        HttpClient client = vertx.createHttpClient();

        String authServerUrl = keycloakConfig.getString("auth-server-url");
        String realmName = keycloakConfig.getString("realm");

        dispatchRequests(HttpMethod.GET, URL.get_user + "/" + userId, null)
            .map(response -> {
                if (StringUtils.isNull(response.toString())) {
                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Invalid user_id.");
                } else {
                    return new JsonObject(response.getDelegate());
                }
            })
            .flatMap(user -> {
                logger.info("Responded user: " + user);
                // Own user_profile can be changed or those users_profiles which is associated with same company
                if (ctxUser.getString("user_id").equals(userId)
                    || (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())
                    && ctxUser.getString("company_id").equals(user.getString("associated_company_id")))) {

                    return UserUtils.updateUser(userId, keycloakUserRepresentation, accessToken, authServerUrl, realmName, client);

                } else {
                    throw forbidden();
                }
            })
            .flatMap(ignored -> UserUtils.getUser(userId, accessToken, authServerUrl, realmName, client))
            .flatMap(keycloakUser -> {
                logger.info("Keycloak user: " + keycloakUser);
                // Permission is already granted in above statement, we don't need to check again
                if (!ctxUser.getString("user_id").equals(userId)) {
                    // Child <Companies> users edition
                    if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                        // Only child <Companies> can be added by the parent
                        return dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject().put("associated_company_id", ctxUser.getString("company_id")))
                            .flatMap(response -> {
                                JsonArray childCompanies = new JsonArray(response.getDelegate());
                                logger.info("Child companies: " + childCompanies);
                                if (childCompanies.size() > 0) {
                                    return updateMongoUser(ctxUser, body, keycloakUser, childCompanies);
                                } else {
                                    // This case shouldn't be happened; otherwise only half operation will be successful
                                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <Company> at first.");
                                }
                            });
                    } else {
                        // Only child <User Groups> can be added by the parent
                        return updateMongoUser(ctxUser, body, keycloakUser, null);
                    }
                } else {
                    return updateOwnUser(body, ctxUser, keycloakUser);
                }
            }).subscribe(statusCode -> ctx.response().setStatusCode(statusCode).end(), throwable -> handleHttpException(throwable, ctx));
    }

    private void handleUpdateSite(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            String siteId = ctx.request().getParam("id");
            dispatchRequests(HttpMethod.GET, URL.get_site + "/" + siteId, new JsonObject())
                .map(buffer -> {
                    if (StringUtils.isNull(buffer.toString()) || buffer.toJsonObject().getString("associated_company_id").equals(ctx.user().principal().getString("company_id"))) {
                        throw forbidden();
                    } else {
                        return new JsonObject(buffer.getDelegate());
                    }
                })
                .flatMap(site -> {
                    JsonObject siteObject = new Site(ctx.getBodyAsJson().put("associated_company_id", ctx.user().principal().getString("company_id"))).toJsonObject()
                        .put("_id", site.getString("_id"));
                    return dispatchRequests(HttpMethod.PUT, URL.put_site, siteObject);
                })
                .subscribe(ignored -> ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(),
                    throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private void handleUpdateUserGroup(RoutingContext ctx) {
        Role role = Role.valueOf(ctx.user().principal().getString("role"));
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            String userGroupId = ctx.request().getParam("id");

            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + userGroupId, new JsonObject())
                .map(buffer -> {
                    if (StringUtils.isNull(buffer.toString()) || buffer.toJsonObject().getString("associated_company_id").equals(ctx.user().principal().getString("company_id"))) {
                        throw forbidden();
                    } else {
                        return new JsonObject(buffer.getDelegate());
                    }
                })
                .flatMap(userGroup ->
                    dispatchRequests(HttpMethod.POST, URL.get_site, new JsonObject().put("associated_company_id", ctx.user().principal().getString("company_id")))
                        .map(buffer -> {
                            JsonArray childCompaniesResponse = new JsonArray(buffer.getDelegate());
                            if (childCompaniesResponse.size() > 0) {
                                String[] availableSites = StringUtils.getIds(childCompaniesResponse);
                                String siteId = SQLUtils.getMatchValueOrDefaultOne(ctx.getBodyAsJson().getString("site_id", ""), availableSites);
                                return new UserGroup(ctx.getBodyAsJson()
                                    .put("associated_company_id", ctx.user().principal().getString("company_id"))
                                    .put("site_id", siteId))
                                    .toJsonObject().put("_id", userGroup.getString("_id"));
                            } else {
                                throw badRequest("Create <Site> at first.");
                            }
                        })
                        .flatMap(userGroupObject -> dispatchRequests(HttpMethod.PUT, URL.put_user_group, userGroupObject))
                ).subscribe(buffer -> ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(), throwable -> handleHttpException(throwable, ctx));
        } else {
            forbidden(ctx);
        }
    }

    private void respondRequestWithSiteAndAssociateCompanyRepresentation(RoutingContext ctx, JsonObject query, String urn) {
        dispatchRequests(HttpMethod.POST, urn, query)
            .flatMap(response -> Observable.fromIterable(response.toJsonArray())
                .flatMapSingle(res -> {
                    JsonObject object = new JsonObject(res.toString());
                    return dispatchRequests(HttpMethod.GET, URL.get_site + "/" + object.getString("site_id"), null)
                        .flatMap(site -> {
                            if (StringUtils.isNotNull(site.toString())) {
                                object.put("site", site.toJsonObject());
                            }
                            return dispatchRequests(HttpMethod.GET, URL.get_company + "/" + object.getString("associated_company_id"), null)
                                .map(associatedCompany -> object.put("associated_company", associatedCompany.toJsonObject()));
                        });
                }).toList()
            )
            .subscribe(response -> {
                    JsonArray array = new JsonArray();
                    response.forEach(array::add);
                    ctx.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .end(Json.encodePrettily(array));
                },
                throwable -> handleHttpException(throwable, ctx));
    }

    private void respondRequestWithAssociateCompanyRepresentation(RoutingContext ctx, JsonObject query, String urn) {
        // We may do optimize version of this
        dispatchRequests(HttpMethod.POST, urn, query)
            .flatMap(response -> Observable.fromIterable(response.toJsonArray())
                .flatMapSingle(res -> {
                    JsonObject object = new JsonObject(res.toString());
                    return dispatchRequests(HttpMethod.GET, URL.get_company + "/" + object.getString("associated_company_id"), null)
                        .map(company -> {
                            if (StringUtils.isNotNull(company.toString())) {
                                object.put("associated_company", company.toJsonObject());
                            }
                            return object;
                        });
                }).toList()
            ).subscribe(response -> {
                JsonArray array = new JsonArray();
                response.forEach(array::add);
                ctx.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(Json.encodePrettily(array));
            },
            throwable -> handleHttpException(throwable, ctx));
    }

    private void respondRequestWithCompanyAssociateCompanyAndGroupRepresentation(RoutingContext ctx, JsonObject query, String urn) {
        // We may do optimize version of this
        dispatchRequests(HttpMethod.POST, urn, query)
            .flatMap(response -> Observable.fromIterable(response.toJsonArray())
                .flatMapSingle(res -> {
                    JsonObject object = new JsonObject(res.toString());
                    return dispatchRequests(HttpMethod.GET, URL.get_company + "/" + object.getString("associated_company_id"), null)
                        .flatMap(associatedCompany -> {
                            if (StringUtils.isNotNull(associatedCompany.toString())) {
                                object.put("associated_company", associatedCompany.toJsonObject());
                            }
                            return dispatchRequests(HttpMethod.GET, URL.get_company + "/" + object.getString("company_id"), null)
                                .flatMap(company -> {
                                    if (StringUtils.isNotNull(company.toString())) {
                                        object.put("company", company.toJsonObject());
                                    }
                                    return dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + object.getString("group_id"), null)
                                        .flatMap(group -> {
                                            if (StringUtils.isNotNull(group.toString())) {
                                                return dispatchRequests(HttpMethod.GET, URL.get_site + "/" + group.toJsonObject().getString("site_id"), null)
                                                    .map(site -> {
                                                        if (StringUtils.isNotNull(site.toString())) {
                                                            object.put("group", group.toJsonObject().put("site", site.toJsonObject()));
                                                        }
                                                        return object;
                                                    });
                                            }
                                            return Single.just(object);
                                        });
                                });
                        });
                }).toList()
            ).subscribe(response -> {
                JsonArray array = new JsonArray();
                response.forEach(array::add);
                ctx.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(Json.encodePrettily(array));
            },
            throwable -> handleHttpException(throwable, ctx));
    }

    private void handleHttpException(Throwable throwable, RoutingContext routingContext) {
        HttpException exception = (HttpException) throwable;
        routingContext.response()
            .setStatusCode(exception.getStatusCode().code())
            .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
            .end(Json.encodePrettily(new JsonObject().put("message", exception.getMessage())));
    }
    //endregion -------------------------------------------------------------------------------------------------------


    // region Dispatch requests ----------------------------------------------------------------------------------------
    protected Single<Buffer> dispatchRequests(HttpMethod method, String path, JsonObject payload) {
        int initialOffset = 5; // length of `/api/`
        // run with circuit breaker in order to deal with failure
        return circuitBreaker.rxExecuteCommand(future -> {
            getRxAllEndpoints().flatMap(recordList -> {
                if (path.length() <= initialOffset) {
                    return Single.error(new HttpException(HttpResponseStatus.BAD_REQUEST, "Not found."));
                }
                String prefix = (path.substring(initialOffset)
                    .split("/"))[0];
                getLogger().info("Prefix: " + prefix);
                // generate new relative path
                String newPath = path.substring(initialOffset + prefix.length());
                // get one relevant HTTP client, may not exist
                getLogger().info("New path: " + newPath);
                Optional<Record> client = recordList.stream()
                    .filter(record -> record.getMetadata().getString("api.name") != null)
                    .filter(record -> record.getMetadata().getString("api.name").equals(prefix))
                    .findAny(); // simple load balance

                if (client.isPresent()) {
                    getLogger().info("Found client for uri: " + path);
                    Single<HttpClient> httpClientSingle = HttpEndpoint.rxGetClient(discovery,
                        rec -> rec.getType().equals(io.vertx.servicediscovery.types.HttpEndpoint.TYPE) && rec.getMetadata().getString("api.name").equals(prefix));
                    return doDispatch(newPath, method, payload, httpClientSingle);
                } else {
                    getLogger().info("Client endpoint not found for uri: " + path);
                    return Single.error(new HttpException(HttpResponseStatus.BAD_REQUEST, "Not found."));
                }
            }).subscribe(future::complete, future::fail);
        });
    }

    /**
     * Dispatch the request to the downstream REST layers.
     */
    private Single<Buffer> doDispatch(String path, HttpMethod method, JsonObject payload, Single<HttpClient> httpClientSingle) {
        return Single.create(source ->
            httpClientSingle.subscribe(client -> {
                HttpClientRequest toReq = client.request(method, path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            source.onError(new HttpException(HttpResponseStatus.valueOf(response.statusCode()), response.statusCode() + ": " + body.toString()));
                            getLogger().info("Failed to dispatch: " + response.toString());
                        } else {
                            source.onSuccess(body);
                            client.close();
                        }
                        io.vertx.servicediscovery.ServiceDiscovery.releaseServiceObject(discovery.getDelegate(), client);
                    });
                });
                toReq.setChunked(true);
                toReq.getDelegate().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                if (payload == null) {
                    toReq.end();
                } else {
                    toReq.write(payload.encode()).end();
                }
            })
        );
    }

    private Single<List<Record>> getRxAllEndpoints() {
        return discovery.rxGetRecords(record -> record.getType().equals(io.vertx.servicediscovery.types.HttpEndpoint.TYPE));
    }

    private void dispatchRequests(RoutingContext context) {
        System.out.println("Dispatch Requests called");
        int initialOffset = 5; // length of `/api/`
        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> {
            getAllEndpoints().setHandler(ar -> {
                if (ar.succeeded()) {
                    List<Record> recordList = ar.result();
                    // get relative path and retrieve prefix to dispatch client
                    String path = context.request().uri();

                    if (path.length() <= initialOffset) {
                        HttpHelper.notFound(context);
                        future.complete();
                        return;
                    }
                    String prefix = (path.substring(initialOffset)
                        .split("/"))[0];
                    System.out.println("prefix = " + prefix);
                    // generate new relative path
                    String newPath = path.substring(initialOffset + prefix.length());
                    // get one relevant HTTP client, may not exist
                    System.out.println("new path = " + newPath);
                    Optional<Record> client = recordList.stream()
                        .filter(record -> record.getMetadata().getString("api.name") != null)
                        .filter(record -> record.getMetadata().getString("api.name").equals(prefix))
                        .findAny(); // simple load balance

                    if (client.isPresent()) {
                        System.out.println("Found client for uri: " + path);
                        Single<HttpClient> httpClientSingle = HttpEndpoint.rxGetClient(discovery,
                            rec -> rec.getType().equals(io.vertx.servicediscovery.types.HttpEndpoint.TYPE) && rec.getMetadata().getString("api.name").equals(prefix));
                        doDispatch(context, newPath, httpClientSingle, future);
                    } else {
                        System.out.println("Client endpoint not found for uri " + path);
                        HttpHelper.notFound(context);
                        future.complete();
                    }
                } else {
                    future.fail(ar.cause());
                }
            });
        }).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });
    }

    /**
     * Dispatch the request to the downstream REST layers.
     *
     * @param context          routing context instance
     * @param path             relative path
     * @param httpClientSingle relevant HTTP client
     */
    private void doDispatch(RoutingContext context, String path, Single<HttpClient> httpClientSingle, Future<Object> cbFuture) {
        httpClientSingle.subscribe(client -> {
            HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                        } else {
                            HttpServerResponse toRsp = context.response().setStatusCode(response.statusCode());
                            response.headers().getDelegate().forEach(header -> {
                                toRsp.putHeader(header.getKey(), header.getValue());
                            });
                            // send response
                            toRsp.end(body);
                            client.close();
                            cbFuture.complete();
                        }
                        ServiceDiscovery.releaseServiceObject(discovery.getDelegate(), client);
                    });
                });
            // set headers
            context.request().headers().getDelegate().forEach(header -> {
                toReq.putHeader(header.getKey(), header.getValue());
            });
            if (context.getBody() == null) {
                toReq.end();
            } else {
                toReq.end(context.getBody());
            }
        });
    }

    /**
     * Get all REST endpoints from the service discovery infrastructure.
     *
     * @return async result
     */
    private Future<List<Record>> getAllEndpoints() {
        Future<List<Record>> future = Future.future();
        discovery.getRecords(record -> record.getType().equals(io.vertx.servicediscovery.types.HttpEndpoint.TYPE),
            future.completer());
        return future;
    }
    // endregion -------------------------------------------------------------------------------------------------------
}
