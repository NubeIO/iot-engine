package com.nubeiot.dashboard.controllers;

import static com.nubeiot.core.http.handler.ResponseDataWriter.responseData;
import static com.nubeiot.core.mongo.MongoUtils.idQuery;
import static com.nubeiot.dashboard.constants.Collection.COMPANY;
import static com.nubeiot.dashboard.constants.Collection.SITE;
import static com.nubeiot.dashboard.constants.Collection.USER;
import static com.nubeiot.dashboard.constants.Collection.USER_GROUP;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.auth.oauth2.AccessToken;
import io.vertx.reactivex.ext.auth.oauth2.OAuth2Auth;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.helper.ResponseDataHelper;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestConfigProvider;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.UserImpl;
import com.nubeiot.dashboard.providers.RestOAuth2AuthProvider;
import com.zandero.rest.annotation.RouteOrder;

public class AuthRestController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    @POST
    @Path("/login/account")
    @RouteOrder(1)
    public Future<ResponseData> login(@Context RoutingContext ctx, @Context RestOAuth2AuthProvider oAuth2AuthProvider) {
        return handleLogin(ctx, oAuth2AuthProvider.getOAuth2Auth());
    }

    @POST
    @Path("/refreshToken")
    @RouteOrder(1)
    public Future<ResponseData> refreshToken(@Context Vertx vertx, @Context RoutingContext ctx,
                                             @Context RestConfigProvider configProvider) {
        return handleRefreshToken(vertx, ctx, configProvider);
    }

    @GET
    @Path("/currentUser")
    @RouteOrder(3)
    public Future<ResponseData> currentUser(@Context RoutingContext ctx,
                                            @Context RestMongoClientProvider mongoClientProvider) {
        return currentUserHandler(ctx, mongoClientProvider.getMongoClient());
    }

    @POST
    @Path("/logout")
    @RouteOrder(3)
    public Future<ResponseData> redirectLogout(@Context Vertx vertx, @Context RoutingContext ctx,
                                               @Context RestConfigProvider configProvider) {
        return redirectLogoutHandler(vertx, ctx, configProvider);
    }

    @GET
    @Path("/*")
    @RouteOrder(2)
    public Future<ResponseData> authMiddlewareGet(@Context RoutingContext ctx,
                                                  @Context RestOAuth2AuthProvider oAuth2AuthProvider,
                                                  @Context RestMongoClientProvider mongoClientProvider) {
        return authMiddlewareHandler(ctx, oAuth2AuthProvider.getOAuth2Auth(), mongoClientProvider.getMongoClient());
    }

    @POST
    @Path("/*")
    @RouteOrder(2)
    public Future<ResponseData> authMiddlewarePost(@Context RoutingContext ctx,
                                                   @Context RestOAuth2AuthProvider oAuth2AuthProvider,
                                                   @Context RestMongoClientProvider mongoClientProvider) {
        return authMiddlewareHandler(ctx, oAuth2AuthProvider.getOAuth2Auth(), mongoClientProvider.getMongoClient());
    }

    @PUT
    @Path("/*")
    @RouteOrder(2)
    public Future<ResponseData> authMiddlewarePut(@Context RoutingContext ctx,
                                                  @Context RestOAuth2AuthProvider oAuth2AuthProvider,
                                                  @Context RestMongoClientProvider mongoClientProvider) {
        return authMiddlewareHandler(ctx, oAuth2AuthProvider.getOAuth2Auth(), mongoClientProvider.getMongoClient());
    }

    @PATCH
    @Path("/*")
    @RouteOrder(2)
    public Future<ResponseData> authMiddlewarePatch(@Context RoutingContext ctx,
                                                    @Context RestOAuth2AuthProvider oAuth2AuthProvider,
                                                    @Context RestMongoClientProvider mongoClientProvider) {
        return authMiddlewareHandler(ctx, oAuth2AuthProvider.getOAuth2Auth(), mongoClientProvider.getMongoClient());
    }

    @DELETE
    @Path("/*")
    @RouteOrder(2)
    public Future<ResponseData> authMiddlewareDelete(@Context RoutingContext ctx,
                                                     @Context RestOAuth2AuthProvider oAuth2AuthProvider,
                                                     @Context RestMongoClientProvider mongoClientProvider) {
        return authMiddlewareHandler(ctx, oAuth2AuthProvider.getOAuth2Auth(), mongoClientProvider.getMongoClient());
    }

    private Future<ResponseData> redirectLogoutHandler(Vertx vertx, RoutingContext ctx,
                                                       RestConfigProvider configProvider) {
        Future<ResponseData> future = Future.future();
        JsonObject body = ctx.getBodyAsJson();
        User user = ctx.user();
        String access_token = user.principal().getString("access_token");
        String refresh_token = body.getString("refresh_token");
        JsonObject keycloakConfig = configProvider.getConfig().getAppConfig().toJson().getJsonObject("keycloak");
        String client_id = keycloakConfig.getString("resource");
        String client_secret = keycloakConfig.getJsonObject("credentials").getString("secret");
        String realmName = keycloakConfig.getString("realm");
        String uri = keycloakConfig.getString("auth-server-url") + "/realms/" + realmName +
                     "/protocol/openid-connect/logout";

        io.vertx.core.http.HttpClient client = vertx.createHttpClient();

        io.vertx.core.http.HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            future.complete(new ResponseData().setStatus(response.statusCode()));
        });
        request.setChunked(true);

        String body$ = "refresh_token=" + refresh_token + "&client_id=" + client_id + "&client_secret=" + client_secret;
        request.putHeader("content-type", "application/x-www-form-urlencoded");
        request.putHeader("Authorization", "Bearer " + access_token);

        request.write(body$).end();
        return future;
    }

    private Future<ResponseData> currentUserHandler(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();

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
                        return assignSiteOnAvailability(ctx, mongoClient, group);
                    }
                });
            } else {
                return assignSiteOnAvailability(ctx, mongoClient, group);
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
            // Use case of header username: ditto NGINX
            ResponseData responseData = responseData(
                user.principal().mergeIn(groupAndSiteAndCompany).encode()).setHeaders(
                new JsonObject().put("username", user.principal().getString("username")));

            future.complete(responseData);
        });
        return future;
    }

    private SingleSource<? extends JsonObject> assignSiteOnAvailability(RoutingContext ctx, MongoClient mongoClient,
                                                                        JsonObject group) {
        String role = ctx.user().principal().getString("role");
        if (Strings.in(role, Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
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

    private Future<ResponseData> handleRefreshToken(Vertx vertx, RoutingContext ctx,
                                                    RestConfigProvider configProvider) {
        Future<ResponseData> future = Future.future();
        ResponseData responseData = new ResponseData();

        JsonObject body = ctx.getBodyAsJson();
        String refresh_token = body.getString("refresh_token");
        String access_token = ctx.request().getHeader("Authorization"); // Bearer {{token}}
        JsonObject keycloakConfig = configProvider.getConfig().getAppConfig().toJson().getJsonObject("keycloak");
        String client_id = keycloakConfig.getString("resource");
        String client_secret = keycloakConfig.getJsonObject("credentials").getString("secret");
        String realmName = keycloakConfig.getString("realm");
        String uri = keycloakConfig.getString("auth-server-url") + "/realms/" + realmName +
                     "/protocol/openid-connect/token";
        HttpClient client = vertx.createHttpClient();

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            response.bodyHandler(body$ -> {
                responseData.setStatus(response.statusCode());
                if (response.statusCode() == 200) {
                    responseData(responseData, body$.toString());
                }
                future.complete(responseData);
            });
        });
        request.setChunked(true);

        String body$ = "refresh_token=" + refresh_token + "&client_id=" + client_id + "&client_secret=" +
                       client_secret + "&grant_type=refresh_token";
        request.putHeader("content-type", "application/x-www-form-urlencoded");
        request.putHeader("Authorization", access_token);

        request.write(body$).end();
        return future;
    }

    private Future<ResponseData> handleLogin(RoutingContext ctx, OAuth2Auth loginAuth) {
        Future<ResponseData> future = Future.future();

        JsonObject body = ctx.getBodyAsJson();
        String username = body.getString("username");
        String password = body.getString("password");

        loginAuth.rxAuthenticate(new JsonObject().put("username", username).put("password", password))
            .subscribe(token -> future.complete(responseData(token.principal().encode())),
                       error -> future.complete(ResponseDataHelper.unauthorized()));
        return future;
    }

    private Future<ResponseData> authMiddlewareHandler(RoutingContext ctx, OAuth2Auth loginAuth,
                                                       MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        logger.info("Auth middleware is being called...");
        String authorization = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null) {
            authorization = authorization.substring("Bearer".length()).trim();
            setAuthenticUser(ctx, loginAuth, mongoClient, authorization, future);
        } else {
            // Web pages WebSocket authentication
            if (Strings.isNotBlank(ctx.request().getHeader("X-Original-URI"))) {
                String[] contents = ctx.request().getHeader("X-Original-URI").split("access_token=");
                if (contents.length == 2) {
                    authorization = contents[1].substring("Bearer%20".length());
                    setAuthenticUser(ctx, loginAuth, mongoClient, authorization, future);
                } else {
                    String[] credentials = ctx.request()
                        .getHeader("X-Original-URI")
                        .replaceFirst("/ws/[^?]*(\\?)?", "")
                        .split(":::");
                    // NodeRED WebSocket authentication
                    if (credentials.length == 2) {
                        loginAuth.rxGetToken(
                            new JsonObject().put("username", credentials[0]).put("password", credentials[1]))
                            .subscribe(token -> {
                                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, HttpUtils.DEFAULT_CONTENT_TYPE)
                                   .putHeader("username", credentials[0])
                                   .end(Json.encodePrettily(token.principal()));
                            }, throwable -> future.complete(ResponseDataHelper.unauthorized()));
                    } else {
                        future.complete(ResponseDataHelper.unauthorized());
                    }
                }
            } else {
                future.complete(ResponseDataHelper.unauthorized());
            }
        }
        return future;
    }

    private void setAuthenticUser(RoutingContext ctx, OAuth2Auth loginAuth, MongoClient mongoClient,
                                  String authorization, Future<ResponseData> future) {
        loginAuth.introspectToken(authorization, res -> {
            if (res.succeeded()) {
                AccessToken token = res.result();

                String username = token.principal().getString("username");
                String access_token = token.principal().getString("access_token");
                mongoClient.rxFindOne(USER, new JsonObject().put("username", username), null).subscribe(response -> {
                    io.vertx.ext.auth.User user = new UserImpl(
                        new JsonObject().put("access_token", access_token).mergeIn(response));
                    ctx.setUser(user);
                    ctx.next();
                }, throwable -> future.complete(ResponseDataHelper.unauthorized()));
            } else {
                future.complete(ResponseDataHelper.unauthorized());
            }
        });
    }

}
