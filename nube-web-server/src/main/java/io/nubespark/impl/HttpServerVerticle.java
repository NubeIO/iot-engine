package io.nubespark.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.Role;
import io.nubespark.impl.models.*;
import io.nubespark.utils.SQLUtils;
import io.nubespark.utils.StringUtils;
import io.nubespark.utils.URN;
import io.nubespark.utils.UserUtils;
import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.RestAPIVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.KeycloakHelper;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static io.nubespark.utils.Constants.SERVICE_NAME;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

/**
 * Created by topsykretts on 5/4/18.
 */
public class HttpServerVerticle extends RestAPIVerticle {

    private OAuth2Auth loginAuth;
    Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    @Override
    public void start() {
        super.start();

        Router router = Router.router(vertx);
        // creating body handler
        router.route().handler(BodyHandler.create());

        loginAuth = KeycloakAuth.create(vertx, OAuth2FlowType.PASSWORD, config().getJsonObject("keycloak"));

        enableCorsSupport(router);
        handleAuth(router);
        handleAPIs(router);
        handleAuthEventBus(router);
        handleEventBus(router);
        handleGateway(router);
        handleStaticResource(router);

        String host = config().getString("host", "localhost");
        int port = config().getInteger("http.port", 8085);

        // By default index.html from webroot/ is available on "/".
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port,
                        handler -> {
                            if (handler.failed()) {
                                handler.cause().printStackTrace();
                                Future.failedFuture(handler.cause());
                            } else {
                                System.out.println("Front End server started on port: " + port);
                                Future.succeededFuture();
                            }
                        }
                );

        publishHttpEndpoint(SERVICE_NAME, host, port, ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Front End Server service published: " + ar.succeeded());
            }
        });

    }

    private void handleGateway(Router router) {
        // api dispatcher
        router.route("/api/*").handler(this::dispatchRequests);
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
                        notFound(context);
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
                        doDispatch(context, newPath, discovery.getReference(client.get()).get(), future);
                    } else {
                        System.out.println("Client endpoint not found for uri " + path);
                        notFound(context);
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
     * @param context routing context instance
     * @param path    relative path
     * @param client  relevant HTTP client
     */
    private void doDispatch(RoutingContext context, String path, HttpClient client, Future<Object> cbFuture) {
        HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                        } else {
                            HttpServerResponse toRsp = context.response()
                                    .setStatusCode(response.statusCode());
                            response.headers().forEach(header -> {
                                toRsp.putHeader(header.getKey(), header.getValue());
                            });
                            System.out.println("Body is=======> " + body);
                            // send response
                            toRsp.end(body);
                            cbFuture.complete();
                        }
                        ServiceDiscovery.releaseServiceObject(discovery, client);
                    });
                });
        // set headers
        context.request().headers().forEach(header -> {
            toReq.putHeader(header.getKey(), header.getValue());
        });
        if (context.getBody() == null) {
            toReq.end();
        } else {
            toReq.end(context.getBody());
        }
    }

    /**
     * Get all REST endpoints from the service discovery infrastructure.
     *
     * @return async result
     */
    private Future<List<Record>> getAllEndpoints() {
        Future<List<Record>> future = Future.future();
        discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE),
                future.completer());
        return future;
    }

    /**
     * An implementation of handling authentication system and response on the authentic URLs only
     *
     * @param router for routing the URLs
     */
    private void handleAuth(Router router) {
        router.route("/api/login/account").handler((RoutingContext ctx) -> {
            JsonObject body = ctx.getBodyAsJson();
            String username = body.getString("username");
            String password = body.getString("password");

            loginAuth.authenticate(new JsonObject().put("username", username).put("password", password), res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                    System.out.println(res.result());
                    failAuthentication(ctx);
                } else {
                    AccessToken token = (AccessToken) res.result();
                    ctx.response()
                            .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(token.principal()));
                }
            });
        });

        router.route("/api/refreshToken").handler(this::refreshAccessToken);

        router.route("/api/*").handler(ctx -> {
            String authorization = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                authorization = authorization.substring("Bearer ".length());
                System.out.println(authorization);
                setAuthenticUser(ctx, authorization);
            } else {
                failAuthentication(ctx);
            }
        });

        router.route("/api/currentUser").handler(ctx -> {
            User user = ctx.user();
            if (user != null) {
                JsonObject accessToken = KeycloakHelper.accessToken(user.principal());
                String name = accessToken.getString("name", accessToken.getString("preferred_username"));
                ctx.response().putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                        .end(Json.encodePrettily(new JsonObject()
                                .put("name", name)
                        ));
            } else {
                System.out.println("Send not authorized error and user should login");
                failAuthentication(ctx);
            }
        });

        router.route("/api/logout").handler(this::redirectLogout);
    }

    private void handleAPIs(Router router) {
        router.post("/api/user").handler(ctx -> {
            // TODO: refactoring code
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
                JsonObject body = ctx.getBodyAsJson();
                JsonObject user = ctx.user().principal();
                KeycloakUserRepresentation userRepresentation = new KeycloakUserRepresentation(body);
                String accessToken = user.getString("access_token");
                JsonObject keycloakConfig = config().getJsonObject("keycloak");
                HttpClient client = vertx.createHttpClient(new HttpClientOptions());

                // 1. Create User on Keycloak
                UserUtils.createUser(userRepresentation, accessToken, keycloakConfig.getString("auth-server-url"), keycloakConfig.getString("realm"), client, res -> {
                    if (res.result().getInteger("statusCode") == 201) {
                        logger.info("Successfully create the user: " + body.getString("username") + " in keycloak.");

                        // 2. GET recently created user details from Keycloak
                        String authServerUrl = keycloakConfig.getString("auth-server-url");
                        String realmName = keycloakConfig.getString("realm");
                        UserUtils.getUser(body.getString("username"), accessToken, authServerUrl, realmName, client, keycloakUser -> {
                            if (keycloakUser.result().getInteger("statusCode") == 200) {
                                String createdUserId = keycloakUser.result().getJsonObject("body").getString("id");
                                logger.info("Created user is::: " + keycloakUser.result().getJsonObject("body"));

                                // 3. Resetting password; by default password: 'helloworld'
                                UserUtils.resetPassword(createdUserId, body.getString("password", "helloworld"),
                                        accessToken, authServerUrl, realmName, client, resetResponse -> {
                                            logger.info("Reset Password statusCode: " + resetResponse.result().getInteger("statusCode"));
                                            if (resetResponse.result().getInteger("statusCode") == 204) {

                                                if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {

                                                    // 4. only child companies can be added by the parent
                                                    getChildCompanies(user.getString("company_id"), responseChildCompanies -> {
                                                        if (responseChildCompanies.result().size() > 0) {

                                                            String[] _ids = StringUtils.getIds(responseChildCompanies.result());
                                                            body.put("company_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("company_id", ""), _ids))
                                                                    .put("associated_company_id", user.getString("company_id"));
                                                            // 5.1 Creating user on MongoDB
                                                            createMongoUser(body, user, keycloakUser.result(), ctx);
                                                        } else {
                                                            // 5.2 Remove user from Keycloak
                                                            deleteKeycloakUser(createdUserId, accessToken, authServerUrl,
                                                                    realmName, client, ctx, "Create company at first.");
                                                        }
                                                    });
                                                } else {

                                                    // 4 Creating user on MongoDB with 'group_id'
                                                    getChildUserGroups(user.getString("company_id"), responseChildUserGroups -> {
                                                        if (responseChildUserGroups.result().size() > 0) {
                                                            String[] _ids = StringUtils.getIds(responseChildUserGroups.result());
                                                            body.put("company_id", user.getString("company_id"))
                                                                    .put("associated_company_id", user.getString("company_id"))
                                                                    .put("group_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("group_id", ""), _ids));
                                                            // 5.1 Creating user on MongoDB
                                                            createMongoUser(body, user, keycloakUser.result(), ctx);
                                                        } else {
                                                            // 5.2 Remove user from Keycloak
                                                            deleteKeycloakUser(createdUserId, accessToken, authServerUrl,
                                                                    realmName, client, ctx, "Create User group at first.");
                                                        }
                                                    });
                                                }
                                            } else {
                                                ctx.response().setStatusCode(resetResponse.result().getInteger("statusCode")).end();
                                            }
                                        });
                            } else {
                                ctx.response().setStatusCode(res.result().getInteger("statusCode")).end();
                            }
                        });
                    } else {
                        logger.info("Failed...");
                        ctx.response().setStatusCode(res.result().getInteger("statusCode")).end();
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.post("/api/company").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                Company company = new Company(ctx.getBodyAsJson(), ctx.user().principal());
                dispatchRequest(HttpMethod.POST, URN.post_company, company.toJsonObject(), ar -> {
                    if (ar.succeeded()) {
                        JsonObject result = new JsonObject(ar.result());
                        // e.g: company already exist; 409 will be returned
                        ctx.response().setStatusCode(result.getInteger("statusCode")).end();
                    } else {
                        serviceUnavailable(ctx, "Error on Company creation.");
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.post("/api/site").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            if (role == Role.MANAGER) {
                Site site = new Site(ctx.getBodyAsJson()
                        .put("associated_company_id", ctx.user().principal().getString("company_id")));
                dispatchRequest(HttpMethod.POST, URN.post_site, site.toJsonObject(), siteResponse -> {
                    if (siteResponse.succeeded()) {
                        ctx.response().setStatusCode(new JsonObject(siteResponse.result()).getInteger("statusCode")).end();
                        siteResponse.result();
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.post("/api/user_group").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            if (role == Role.MANAGER) {
                // Only manager's sites should make available for user_group
                getChildSites(ctx.user().principal().getString("company_id"), childCompaniesResponse -> {
                    if (childCompaniesResponse.succeeded()) {
                        if (childCompaniesResponse.result().size() > 0) {
                            String[] availableSites = StringUtils.getIds(childCompaniesResponse.result());
                            String site_id = SQLUtils.getMatchValueOrDefaultOne(ctx.getBodyAsJson().getString("site_id", ""), availableSites);
                            UserGroup userGroup = new UserGroup(ctx.getBodyAsJson()
                                    .put("associated_company_id", ctx.user().principal().getString("company_id"))
                                    .put("site_id", site_id));
                            dispatchRequest(HttpMethod.POST, URN.post_user_group, userGroup.toJsonObject(), userGroupResponse -> {
                                if (userGroupResponse.succeeded()) {
                                    ctx.response().setStatusCode(new JsonObject(userGroupResponse.result()).getInteger("statusCode")).end();
                                } else {
                                    serviceUnavailable(ctx);
                                }
                            });
                        } else {
                            ctx.response()
                                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                                    .end(new JsonObject().put("message", "Create site at first").toString());
                        }
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.get("/api/companies").handler(ctx -> {
            getChildCompanies(ctx.user().principal().getString("company_id"), res -> {
                if (res.succeeded()) {
                    ctx.response()
                            .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .end(res.result().toBuffer());
                } else {
                    serviceUnavailable(ctx);
                }
            });
        });

        router.get("/api/users").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
                // GET all users which is associated with the user's company id
                JsonObject query = new JsonObject().put("associated_company_id", ctx.user().principal().getString("company_id"));
                logger.info("Query to be executed: " + query);
                dispatchRequest(HttpMethod.POST, URN.get_user, query, usersResponse -> {
                    if (usersResponse.succeeded()) {
                        logger.info("User response result::: " + usersResponse.result());
                        ctx.response()
                                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(usersResponse.result());
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.get("/api/sites").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            if (role == Role.MANAGER) {
                getChildSites(ctx.user().principal().getString("company_id"), handler -> {
                    if (handler.succeeded()) {
                        ctx.response()
                                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(handler.result().toString());
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.get("/api/user_groups").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            if (role == Role.MANAGER) {
                getChildUserGroups(ctx.user().principal().getString("company_id"), handler -> {
                    if (handler.succeeded()) {
                        ctx.response()
                                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                                .setStatusCode(HttpResponseStatus.OK.code())
                                .end(handler.result().toString());
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.post("/api/delete_users").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            // Model level permission; this is limited to SUPER_ADMIN and ADMIN
            if (role == Role.MANAGER) {
                JsonArray queryInput = ctx.getBodyAsJsonArray();
                // Object level permission
                JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
                dispatchRequest(HttpMethod.POST, URN.get_user, query, usersResponse -> {
                    if (usersResponse.succeeded()) {
                        JsonArray userGroups = new JsonArray(usersResponse.result());
                        if (userGroups.size() == queryInput.size()) {
                            String companyId = ctx.user().principal().getString("company_id");
                            boolean objectLevelPermission = true;
                            for (Object userResponse : userGroups) {
                                JsonObject user = (JsonObject) (userResponse);
                                if (!user.getString("associated_company_id").equals(companyId)) {
                                    objectLevelPermission = false;
                                }
                            }
                            if (objectLevelPermission) {
                                // Authorized
                                // Deleting user from Keycloak
                                for (Object userResponse : userGroups) {
                                    JsonObject user = (JsonObject) (userResponse);
                                    JsonObject keycloakConfig = config().getJsonObject("keycloak");
                                    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
                                    UserUtils.deleteUser(user.getString("_id"),
                                            ctx.user().principal().getString("access_token"),
                                            keycloakConfig.getString("auth-server-url"),
                                            keycloakConfig.getString("realm"), client,
                                            deleteUserKeycloakResponse -> {
                                                if (deleteUserKeycloakResponse.result().getInteger("statusCode") == HttpResponseStatus.NO_CONTENT.code()) {
                                                    // Deleting one by one from MongoDB
                                                    JsonObject queryToDeleteOne = new JsonObject().put("_id", new JsonObject()
                                                            .put("$in", new JsonArray().add(user.getString("_id"))));
                                                    dispatchRequest(HttpMethod.POST, URN.delete_user, queryToDeleteOne, deleteUserResponse -> {
                                                        if (deleteUserResponse.succeeded()) {
                                                            ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                                                        } else {
                                                            serviceUnavailable(ctx);
                                                        }
                                                    });
                                                } else {
                                                    internalError(ctx, new Throwable("<Users> are unable to deleted from the services."));
                                                }
                                            });
                                }
                            } else {
                                forbidden(ctx);
                            }
                        } else {
                            badRequest(ctx, new Throwable("Doesn't have those <Users> on Database."));
                        }
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.post("/api/delete_companies").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            // Model level permission; this is limited to SUPER_ADMIN and ADMIN
            if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                JsonArray queryInput = ctx.getBodyAsJsonArray();
                // Object level permission
                JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
                logger.info("Query=====>" + query);
                dispatchRequest(HttpMethod.POST, URN.get_company, query, companiesResponse -> {
                    if (companiesResponse.succeeded()) {
                        JsonArray companies = new JsonArray(companiesResponse.result());
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
                                // Authorized
                                dispatchRequest(HttpMethod.POST, URN.delete_company, query, deleteCompaniesResponse -> {
                                    if (deleteCompaniesResponse.succeeded()) {
                                        ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                                    } else {
                                        serviceUnavailable(ctx);
                                    }
                                });
                            } else {
                                forbidden(ctx);
                            }

                        } else {
                            badRequest(ctx, new Throwable("Doesn't have those <Companies> on Database."));
                        }
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.post("/api/delete_sites").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            // Model level permission; this is limited to SUPER_ADMIN and ADMIN
            if (role == Role.MANAGER) {
                JsonArray queryInput = ctx.getBodyAsJsonArray();
                // Object level permission
                JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
                dispatchRequest(HttpMethod.POST, URN.get_site, query, sitesResponse -> {
                    if (sitesResponse.succeeded()) {
                        JsonArray sites = new JsonArray(sitesResponse.result());
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
                                // Authorized
                                dispatchRequest(HttpMethod.POST, URN.delete_site, query, deleteSitesResponse -> {
                                    if (deleteSitesResponse.succeeded()) {
                                        ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                                    } else {
                                        serviceUnavailable(ctx);
                                    }
                                });
                            } else {
                                forbidden(ctx);
                            }
                        } else {
                            badRequest(ctx, new Throwable("Doesn't have those <Sites> on Database."));
                        }
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });

        router.post("/api/delete_user_groups").handler(ctx -> {
            Role role = Role.valueOf(ctx.user().principal().getString("role"));
            // Model level permission; this is limited to SUPER_ADMIN and ADMIN
            if (role == Role.MANAGER) {
                JsonArray queryInput = ctx.getBodyAsJsonArray();
                // Object level permission
                JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
                dispatchRequest(HttpMethod.POST, URN.get_user_group, query, userGroupsResponse -> {
                    if (userGroupsResponse.succeeded()) {
                        JsonArray userGroups = new JsonArray(userGroupsResponse.result());
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
                                // Authorized
                                dispatchRequest(HttpMethod.POST, URN.delete_user_group, query, deleteUserGroupsResponse -> {
                                    if (deleteUserGroupsResponse.succeeded()) {
                                        ctx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                                    } else {
                                        serviceUnavailable(ctx);
                                    }
                                });
                            } else {
                                forbidden(ctx);
                            }
                        } else {
                            badRequest(ctx, new Throwable("Doesn't have those <User Groups> on Database."));
                        }
                    } else {
                        serviceUnavailable(ctx);
                    }
                });
            } else {
                forbidden(ctx);
            }
        });
    }

    private void handleAuthEventBus(Router router) {
        router.route("/eventbus/*").handler((RoutingContext ctx) -> {
            String authorization = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Basic")) {
                handleBasicAuth(ctx, authorization);
            } else {
                setAuthenticUser(ctx, ctx.request().getParam("access_token"));
            }
        });
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
                System.out.println("Token: " + token.principal());

                String user_id = token.principal().getString("sub");
                String access_token = token.principal().getString("access_token");
                logger.info("User id: " + user_id);
                dispatchRequest(HttpMethod.GET, URN.get_user + "/" + user_id, null,
                        ar -> {
                            if (ar.succeeded()) {
                                JsonObject result = new JsonObject(ar.result());
                                logger.info("User Response: " + ar.result().toString());
                                User user = new UserImpl(new JsonObject()
                                        .put("user_id", user_id)
                                        .put("role", result.getString("role"))
                                        .put("company_id", result.getString("company_id", ""))
                                        .put("group_id", result.getString("group_id", ""))
                                        .put("access_token", access_token));
                                ctx.setUser(user);
                                ctx.next();
                            } else {
                                logger.info("User Extraction failure");
                                serviceUnavailable(ctx, "Error on user extraction.");
                            }
                        });
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

        HttpClient client = vertx.createHttpClient(new HttpClientOptions());

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
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            response.bodyHandler(body$ -> {
                if (response.statusCode() != 200) {
                    ctx.response().setStatusCode(response.statusCode()).end();
                } else {
                    HttpServerResponse toRsp = ctx.response()
                            .setStatusCode(response.statusCode());
                    response.headers().forEach(header -> {
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

    private void handleBasicAuth(RoutingContext ctx, String authorization) {
        if (authorization != null && authorization.startsWith("Basic")) {
            authorization = authorization.substring("Basic ".length());
            byte decodedAuthorization[] = Base64.getDecoder().decode(authorization);
            String basicAuthString = new String(decodedAuthorization, StandardCharsets.UTF_8);
            String username = basicAuthString.split(":")[0];
            String password = basicAuthString.split(":")[1];
            loginAuth.authenticate(new JsonObject().put("username", username).put("password", password), res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                    System.out.println(res.result());
                    failAuthentication(ctx);
                } else {
                    System.out.println("Basic Authorization passed !!");
                    ctx.next();
                }
            });
        }
    }

    private void getChildCompanies(String companyId, Handler<AsyncResult<JsonArray>> handler) {
        JsonObject query = new JsonObject().put("associated_company_id", companyId);
        dispatchRequest(HttpMethod.POST, URN.get_company, query, responseCompany -> {
            if (responseCompany.succeeded()) {
                handler.handle(Future.succeededFuture(new JsonArray(responseCompany.result())));
            } else {
                handler.handle(Future.failedFuture(responseCompany.cause()));
            }
        });
    }

    private void getChildSites(String companyId, Handler<AsyncResult<JsonArray>> handler) {
        JsonObject query = new JsonObject().put("associated_company_id", companyId);
        dispatchRequest(HttpMethod.POST, URN.get_site, query, siteResponse -> {
            if (siteResponse.succeeded()) {
                handler.handle(Future.succeededFuture(new JsonArray(siteResponse.result())));
            } else {
                handler.handle(Future.failedFuture(siteResponse.cause()));
            }
        });
    }

    private void getChildUserGroups(String companyId, Handler<AsyncResult<JsonArray>> handler) {
        JsonObject query = new JsonObject().put("associated_company_id", companyId);
        dispatchRequest(HttpMethod.POST, URN.get_user_group, query, childUserGroups -> {
            if (childUserGroups.succeeded()) {
                handler.handle(Future.succeededFuture(new JsonArray(childUserGroups.result())));
            } else {
                handler.handle(Future.failedFuture(childUserGroups.cause()));
            }
        });
    }

    private void deleteKeycloakUser(String createdUserId, String accessToken, String authServerUrl, String realmName,
                                    HttpClient client, RoutingContext ctx, String message) {
        UserUtils.deleteUser(createdUserId, accessToken, authServerUrl, realmName, client, deleteUserHandler -> {
            ctx.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end((new JsonObject().put("message", message)).toBuffer());
        });
    }

    private void createMongoUser(JsonObject body, JsonObject user, JsonObject keycloakUser, RoutingContext ctx) {
        MongoUser mongoUser = new MongoUser(body, user, keycloakUser.getJsonObject("body"));
        logger.info("Mongo User::: " + mongoUser.toJsonObject());
        dispatchRequest(HttpMethod.POST, URN.post_user, mongoUser.toJsonObject(), mongoResponse -> {
            if (mongoResponse.succeeded()) {
                logger.info("User creation on MongoDB: " + mongoResponse.result());
                ctx.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
            } else {
                ctx.fail(mongoResponse.cause());
            }
        });
    }
}
