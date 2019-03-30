package com.nubeiot.dashboard.controllers;

import static com.nubeiot.core.mongo.MongoUtils.idQuery;
import static com.nubeiot.dashboard.constants.Collection.COMPANY;
import static com.nubeiot.dashboard.constants.Collection.SITE;
import static com.nubeiot.dashboard.constants.Collection.USER;
import static com.nubeiot.dashboard.constants.Collection.USER_GROUP;
import static com.nubeiot.dashboard.helpers.MultiTenantPermissionHelper.checkPermissionAndReturnValue;
import static com.nubeiot.dashboard.helpers.MultiTenantQueryBuilderHelper.byAdminCompanyGetAdminWithManagerSelectionList;
import static com.nubeiot.dashboard.helpers.MultiTenantQueryBuilderHelper.byAdminCompanyGetAdminWithManagerSelectionListQuery;
import static com.nubeiot.dashboard.helpers.MultiTenantRepresentationHelper.userRepresentation;
import static com.nubeiot.dashboard.utils.UserUtils.getCompanyId;
import static com.nubeiot.dashboard.utils.UserUtils.getRole;
import static com.nubeiot.dashboard.utils.UserUtils.hasClientLevelRole;
import static com.nubeiot.dashboard.utils.UserUtils.hasUserLevelRole;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.http.RestConfigProvider;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.utils.RequestDataConverter;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.mongo.MongoUtils;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.core.utils.SQLUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.DashboardServerConfig;
import com.nubeiot.dashboard.MultiTenantUserProps;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.constants.Services;
import com.nubeiot.dashboard.helpers.ResponseDataHelper;
import com.nubeiot.dashboard.models.KeycloakUserRepresentation;
import com.nubeiot.dashboard.models.MongoUser;
import com.nubeiot.dashboard.providers.RestMicroContextProvider;
import com.nubeiot.dashboard.utils.UserUtils;
import com.zandero.rest.annotation.RouteOrder;

@Path("/api")
public class MultiTenantUserController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(MultiTenantUserController.class);

    @GET
    @Path("/users")
    @RouteOrder(3)
    public Future<ResponseData> get(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleGetUsers(ctx, mongoClient.getMongoClient());
    }

    @POST
    @Path("/user")
    @RouteOrder(3)
    public Future<ResponseData> post(@Context Vertx vertx, @Context RoutingContext ctx,
                                     @Context RestMicroContextProvider microContextProvider,
                                     @Context RestMongoClientProvider mongoClientProvider,
                                     @Context RestConfigProvider configProvider) {
        JsonObject appConfig = configProvider.getAppConfig();
        JsonObject keycloakConfig = appConfig.getJsonObject("keycloak");
        return handlePostUser(vertx, ctx, microContextProvider.getMicroContext(), mongoClientProvider.getMongoClient(),
                              keycloakConfig, appConfig);
    }

    @PATCH
    @Path("/user/:id")
    @RouteOrder(3)
    public Future<ResponseData> patch(@Context Vertx vertx, @Context RoutingContext ctx,
                                      @Context RestMongoClientProvider mongoClientProvider,
                                      @Context RestConfigProvider configProvider) {
        JsonObject keycloakConfig = configProvider.getAppConfig().getJsonObject("keycloak");
        return handlePatchUser(vertx, ctx, mongoClientProvider.getMongoClient(),
                               keycloakConfig);
    }

    @POST
    @Path("/delete_users")
    @RouteOrder(3)
    public Future<ResponseData> delete(@Context Vertx vertx, @Context RoutingContext ctx,
                                       @Context RestMicroContextProvider microContextProvider,
                                       @Context RestMongoClientProvider mongoClientProvider,
                                       @Context RestConfigProvider configProvider) {
        JsonObject appConfig = configProvider.getAppConfig();
        JsonObject keycloakConfig = appConfig.getJsonObject("keycloak");
        return handleDeleteUsers(vertx, ctx, microContextProvider.getMicroContext(),
                                 mongoClientProvider.getMongoClient(), keycloakConfig, appConfig);
    }

    private Future<ResponseData> handleDeleteUsers(Vertx vertx, RoutingContext ctx,
                                                   MicroContext microContext, MongoClient mongoClient,
                                                   JsonObject keycloakConfig, JsonObject appConfig) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        Role role = getRole(user);
        JsonArray arrayBody = ctx.getBodyAsJsonArray();

        MultiTenantUserProps userProps = MultiTenantUserProps.builder()
            .mongoClient(mongoClient)
            .httpClient(vertx.createHttpClient())
            .microContext(microContext)
            .authServerUrl(keycloakConfig.getString("auth-server-url"))
            .realmName(keycloakConfig.getString("realm"))
            .accessToken(user.getString("access_token"))
            .companyId(getCompanyId(user))
            .user(user)
            .role(role)
            .appConfig(appConfig)
            .arrayBody(arrayBody)
            .build();

        // Model level permission; this is limited to SUPER_ADMIN, ADMIN and MANAGER
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", userProps.getArrayBody()));

            mongoClient.rxFind(USER, query)
                .flatMap(users -> {
                    if (users.size() == userProps.getArrayBody().size()) {
                        return checkPermissionAndReturnValue(userProps.getMongoClient(), userProps.getCompanyId(),
                                                             userProps.getRole(), users);
                    }
                    throw HttpException.badRequest("Database doesn't have those Users.");
                })
                .flatMap(users -> Observable.fromIterable(users)
                    .flatMapSingle(usr -> deleteUser(userProps, usr))
                    .toList())
                .subscribe(
                    ignored -> future.complete(new ResponseData().setStatusCode(HttpResponseStatus.NO_CONTENT.code())),
                    throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        } else {
            throw HttpException.forbidden();
        }
        return future;
    }

    private Single<Integer> deleteUser(MultiTenantUserProps userProps, JsonObject user) {
        userProps.setParamsUserId(user.getString("_id")); // for making it to delete

        return UserUtils.deleteUser(userProps).flatMap(deleteUserKeycloakResponse -> {
            if (deleteUserKeycloakResponse.getInteger("statusCode") == HttpResponseStatus.NO_CONTENT.code()) {
                JsonArray $in = new JsonArray().add(user.getString("_id"));
                JsonObject deleteOneQuery = new JsonObject().put("_id", new JsonObject().put("$in", $in));
                DashboardServerConfig dashboardServerConfig =
                    IConfig.from(userProps.getAppConfig(), DashboardServerConfig.class);

                return userProps.getMongoClient().rxRemoveDocuments(USER, deleteOneQuery).flatMap(ignored -> {
                    if (dashboardServerConfig.getDittoPolicy()) {
                        return removeUserOnDittoPolicy(userProps, user);
                    } else {
                        return Single.just(true);
                    }
                }).map(deleteUserResponse -> HttpResponseStatus.NO_CONTENT.code());
            } else {
                throw new HttpException(deleteUserKeycloakResponse.getInteger("statusCode"),
                                        "Users are unable to deleted from the services.");
            }
        });
    }

    private SingleSource<?> removeUserOnDittoPolicy(MultiTenantUserProps userProps, JsonObject user) {
        if (Role.ADMIN == getRole(user)) {
            return byAdminCompanyGetAdminWithManagerSelectionListQuery(userProps.getMongoClient(), getCompanyId(user))
                .flatMap(query -> userProps.getMongoClient().rxFind(SITE, query))
                .flatMap(sites -> Observable.fromIterable(sites)
                    .flatMapSingle(site -> {
                        String path = Services.POLICY_PREFIX + site.getString("_id")
                                      + "/entries/admin/subjects/nginx:" + user.getString("username");
                        return dispatchRequests(userProps.getMicroContext(), HttpMethod.DELETE, path,
                                                userProps.getRequestData());
                    }).toList());
        } else {
            String entry = user.getString("role").toLowerCase();
            if (entry.equals(Role.GUEST.toString().toLowerCase())) {
                entry = "user";
            }
            String path = Services.POLICY_PREFIX + user.getString("site_id") + "/entries/" + entry +
                          "/subjects/nginx:" + user.getString("username");
            return dispatchRequests(userProps.getMicroContext(), HttpMethod.DELETE, path, userProps.getRequestData());
        }
    }

    private Future<ResponseData> handlePatchUser(Vertx vertx, RoutingContext ctx, MongoClient mongoClient,
                                                 JsonObject keycloakConfig) {

        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        Role role = getRole(user);
        JsonObject body = ctx.getBodyAsJson();

        MultiTenantUserProps userProps = MultiTenantUserProps.builder()
            .mongoClient(mongoClient)
            .httpClient(vertx.createHttpClient())
            .authServerUrl(keycloakConfig.getString("auth-server-url"))
            .realmName(keycloakConfig.getString("realm"))
            .accessToken(user.getString("access_token"))
            .companyId(getCompanyId(user))
            .user(user)
            .role(role)
            .body(body)
            .paramsUserId(ctx.request().getParam("id"))
            .keycloakUser(new KeycloakUserRepresentation(body).toJsonObject())
            .build();

        getUserFromParams(mongoClient, userProps)
            .flatMap(usr -> updateKeycloakUser(role, userProps, usr))
            .flatMap(ignored -> UserUtils.getUser(userProps))
            .flatMap(keycloakUser -> patchMongoUser(userProps, keycloakUser))
            .subscribe(statusCode -> future.complete(new ResponseData().setStatusCode(statusCode)),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Single<JsonObject> getUserFromParams(MongoClient mongoClient, MultiTenantUserProps userProps) {
        return mongoClient.rxFindOne(USER, idQuery(userProps.getParamsUserId()), null)
            .map(response -> {
                if (response == null) {
                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Invalid user_id.");
                } else {
                    return response;
                }
            });
    }

    private Single<Integer> patchMongoUser(MultiTenantUserProps userProps, JsonObject keycloakUser) {
        logger.info("Keycloak user: " + keycloakUser);
        userProps.setKeycloakUser(keycloakUser);
        // Permission is already granted in above statement, we don't need to check again
        if (!userProps.getUser().getString("user_id").equals(userProps.getParamsUserId())) {
            // Child <Companies> users edition
            return patchOtherMongoUser(userProps);
        } else {
            return patchOwnMongoUser(userProps);
        }
    }

    private Single<Integer> patchOwnMongoUser(MultiTenantUserProps userProps) {
        // User doesn't have the authority to update own company_id, associated_company_id, and group_id
        JsonObject body = userProps.getBody();
        body.put("company_id", userProps.getUser().getString("company_id"))
            .put("associated_company_id", userProps.getUser().getString("associated_company_id"))
            .put("site_id", userProps.getUser().getString("site_id", ""))
            .put("group_id", userProps.getUser().getString("group_id", ""))
            .put("role", userProps.getUser().getString("role"));
        MongoUser mongoUser = new MongoUser(body, userProps.getUser(), userProps.getKeycloakUser());
        JsonObject mongoUserObject = mongoUser.toJsonObject()
            .put("role", userProps.getUser().getString("role")); // Role shouldn't be overridden
        return userProps.getMongoClient().rxSave(USER, mongoUserObject)
            .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
    }

    private Single<Integer> patchOtherMongoUser(MultiTenantUserProps userProps) {
        if (userProps.getRole() == Role.SUPER_ADMIN) {
            JsonObject query = new JsonObject()
                .put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString())));

            logger.info("Going to patch user with body: {}", userProps.getBody());
            return validateMongoUser(userProps, query)
                .flatMap(editedBody -> {
                    userProps.setBody(editedBody);
                    return updateMongoUser(userProps);
                });
        } else if (userProps.getRole() == Role.ADMIN) {
            return byAdminCompanyGetAdminWithManagerSelectionListQuery(userProps.getMongoClient(),
                                                                       userProps.getCompanyId())
                .flatMap(query -> validateMongoUser(userProps, query)
                    .flatMap(editedBody -> {
                        userProps.setBody(editedBody);
                        return updateMongoUser(userProps);
                    }));
        } else {
            return validateMongoUserForManager(userProps)
                .flatMap(editedBody -> {
                    userProps.setBody(editedBody);
                    return updateMongoUser(userProps);
                });
        }
    }

    private SingleSource<? extends Buffer> updateKeycloakUser(Role role, MultiTenantUserProps userProps,
                                                              JsonObject usr) {
        // Own user_profile can be changed or those users_profiles which is associated with same company
        if (userProps.getUser().getString("user_id").equals(userProps.getParamsUserId())
            || (role == Role.MANAGER) &&
               userProps.getUser().getString("company_id").equals(usr.getString("associated_company_id"))
            || role == Role.SUPER_ADMIN) {
            return UserUtils.updateUser(userProps);
        } else if (role == Role.ADMIN) {
            return byAdminCompanyGetAdminWithManagerSelectionList(userProps.getMongoClient(), usr.getString(
                "company_id"))
                .flatMap(response -> {
                    if (response.contains(usr.getString("company_id"))) {
                        return UserUtils.updateUser(userProps);
                    } else {
                        throw HttpException.forbidden();
                    }
                });
        } else {
            throw HttpException.forbidden();
        }
    }

    private Future<ResponseData> handlePostUser(Vertx vertx, RoutingContext ctx, MicroContext microContext,
                                                MongoClient mongoClient,
                                                JsonObject keycloakConfig, JsonObject appConfig) {
        Future<ResponseData> future = Future.future();

        JsonObject user = ctx.user().principal();
        Role role = getRole(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonObject body = ctx.getBodyAsJson();

            MultiTenantUserProps userProps = MultiTenantUserProps.builder()
                .mongoClient(mongoClient)
                .httpClient(vertx.createHttpClient())
                .microContext(microContext)
                .authServerUrl(keycloakConfig.getString("auth-server-url"))
                .realmName(keycloakConfig.getString("realm"))
                .accessToken(user.getString("access_token"))
                .companyId(getCompanyId(user))
                .user(user)
                .role(role)
                .body(body)
                .appConfig(appConfig)
                .requestData(RequestDataConverter.convert(ctx))
                .keycloakUser(new KeycloakUserRepresentation(body).toJsonObject())
                .build();

            // 1. Create User on Keycloak
            UserUtils.createUser(userProps)
                // 2. GET recently created user details from Keycloak
                .flatMap(ignored -> UserUtils.getUserFromUsername(userProps))
                // 3. Resetting password
                .flatMap(keycloakUser -> {
                    userProps.setKeycloakUser(keycloakUser);
                    return UserUtils.resetPassword(userProps)
                        .flatMap(ignored -> createMongoUser(userProps))
                        .doOnError(t -> {
                            // 5.2 Remove user from Keycloak
                            UserUtils.deleteUser(userProps).subscribe();
                        });
                })
                .subscribe(statusCode -> future.complete(new ResponseData().setStatusCode(statusCode)),
                           throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        }
        return future;
    }

    private SingleSource<Integer> createMongoUser(MultiTenantUserProps userProps) {
        if (userProps.getRole() == Role.SUPER_ADMIN) {
            // 4.1 any user can be created
            JsonObject query = new JsonObject()
                .put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString())));

            return validateMongoUser(userProps, query).flatMap(editedBody -> {
                userProps.setBody(editedBody);
                return saveMongoUser(userProps);
            });
        } else if (userProps.getRole() == Role.ADMIN) {
            // 4.2 only child companies can make associate with it's users
            return byAdminCompanyGetAdminWithManagerSelectionListQuery(userProps.getMongoClient(),
                                                                       userProps.getCompanyId())
                .flatMap(query -> validateMongoUser(userProps, query).flatMap(editedBody -> {
                    userProps.setBody(editedBody);
                    return saveMongoUser(userProps);
                }));
        } else {
            // 4.3 Creating user on MongoDB with 'group_id'
            return validateMongoUserForManager(userProps).flatMap(editedBody -> {
                userProps.setBody(editedBody);
                return saveMongoUser(userProps);
            });
        }
    }

    private Single<JsonObject> validateMongoUser(MultiTenantUserProps userProps, JsonObject query) {
        return userProps.getMongoClient().rxFind(COMPANY, query).flatMap(childCompanies -> {
            if (childCompanies.size() > 0) {
                // 5.1 Proceed for creating MongoDB user
                String[] childCompaniesIds = MongoUtils.getIds(childCompanies);
                // If company doesn't match with user request, a random company will be assigned
                String companyId = SQLUtils
                    .getMatchValueOrFirstOne(userProps.getBody().getString("company_id", ""), childCompaniesIds);
                JsonObject company = MongoUtils.getMatchValueOrFirstOne(childCompanies, companyId);

                JsonObject body = userProps.getBody();
                body.put("company_id", company.getString("_id"));
                userProps.setBody(body);
                logger.info("Now Body became: {}", body);

                if (company.getString("role").equals(Role.MANAGER.toString()) &&
                    hasClientLevelRole(userProps.getBodyRole())) {
                    return validateClientLevelMongoUser(userProps, company);
                } else if (company.getString("role").equals(Role.ADMIN.toString())) {
                    return Single.just(
                        body.put("associated_company_id", company.getString("associated_company_id"))
                            .put("role", company.getString("role"))
                            .put("site_id", "")
                            .put("group_id", ""));
                } else {
                    throw HttpException.badRequest("Condition doesn't match up.");
                }
            } else {
                throw HttpException.badRequest("Create <Company> at first.");
            }
        });
    }

    private Single<JsonObject> validateMongoUserForManager(MultiTenantUserProps userProps) {
        JsonObject query = new JsonObject().put("associated_company_id", userProps.getCompanyId());
        return userProps.getMongoClient().rxFind(USER_GROUP, query).flatMap(childGroups -> {
            if (childGroups.size() > 0) {
                JsonObject body = userProps.getBody();
                // 5.1 Creating user on MongoDB
                body.put("company_id", userProps.getCompanyId())
                    .put("associated_company_id", userProps.getCompanyId())
                    .put("site_id", userProps.getUser().getString("site_id"))
                    .put("group_id", SQLUtils.getMatchValueOrFirstOne(userProps.getBodyGroupId(),
                                                                      MongoUtils.getIds(childGroups)));
                return Single.just(body);
            } else {
                // 5.2 Remove user from Keycloak
                throw HttpException.badRequest("Create <User Group> at first.");
            }
        });
    }

    private Single<Integer> saveMongoUser(MultiTenantUserProps userProps) {
        DashboardServerConfig dashboardServerConfig =
            IConfig.from(userProps.getAppConfig(), DashboardServerConfig.class);
        JsonObject mongoUser = new MongoUser(userProps.getBody(), userProps.getUser(), userProps.getKeycloakUser())
            .toJsonObject();
        return userProps.getMongoClient().rxSave(USER, mongoUser).flatMap(ignored -> {
            if (dashboardServerConfig.getDittoPolicy()) {
                return addUserOnDittoPolicy(userProps, mongoUser);
            } else {
                return Single.just(true);
            }
        }).map(ignored -> HttpResponseStatus.CREATED.code());
    }

    private SingleSource<?> addUserOnDittoPolicy(MultiTenantUserProps userProps, JsonObject mongoUser) {
        if (Role.ADMIN == getRole(mongoUser)) {
            return byAdminCompanyGetAdminWithManagerSelectionListQuery(userProps.getMongoClient(),
                                                                       getCompanyId(mongoUser))
                .flatMap(subQuery -> userProps.getMongoClient().rxFind(SITE, subQuery))
                .flatMap(sites -> Observable.fromIterable(sites)
                    .flatMapSingle(site -> putSubjectOnPolicy(userProps, mongoUser, site.getString("_id")))
                    .toList());
        } else {
            return putSubjectOnPolicy(userProps, mongoUser);
        }
    }

    private Single<Integer> updateMongoUser(MultiTenantUserProps userProps) {
        JsonObject mongoUser = new MongoUser(userProps.getBody(), userProps.getUser(), userProps.getKeycloakUser())
            .toJsonObject();
        return userProps.getMongoClient().rxSave(USER, mongoUser)
            .map(ignored -> HttpResponseStatus.NO_CONTENT.code());
    }

    private Single<ResponseData> putSubjectOnPolicy(MultiTenantUserProps userProps, JsonObject mongoUser,
                                                    String siteId) {
        String path = Services.POLICY_PREFIX + siteId + "/entries/admin/subjects/nginx:" +
                      mongoUser.getString("username");
        RequestData requestData = userProps.getRequestData();
        requestData.setBody(new JsonObject().put("type", "admin"));
        return dispatchRequests(
            userProps.getMicroContext(), HttpMethod.PUT, path, requestData);
    }

    private SingleSource<?> putSubjectOnPolicy(MultiTenantUserProps userProps, JsonObject mongoUser) {
        String entry = mongoUser.getString("role").toLowerCase();
        if (entry.equals(Role.GUEST.toString().toLowerCase())) {
            entry = "user";
        }
        String path = Services.POLICY_PREFIX + mongoUser.getString("site_id") + "/entries/" + entry +
                      "/subjects/nginx:" + mongoUser.getString("username");
        RequestData requestData = userProps.getRequestData();
        requestData.setBody(new JsonObject().put("type", mongoUser.getString("role").toLowerCase()));
        return dispatchRequests(userProps.getMicroContext(), HttpMethod.PUT, path, userProps.getRequestData());
    }

    // TODO: testing and change
    private Single<ResponseData> dispatchRequests(MicroContext microContext, HttpMethod method, String path,
                                                  RequestData requestData) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        int initialOffset = 5; // length of `/api/`
        if (path.length() <= initialOffset) {
            return Single.error(new HttpException(HttpResponseStatus.BAD_REQUEST, "Not found"));
        }
        String prefix = (path.substring(initialOffset).split("/"))[0];
        logger.info("Prefix: {}", prefix);
        String newPath = path.substring(initialOffset + prefix.length());
        logger.info("New path: {}", newPath);
        return microContext.getClusterController()
            .executeHttpService(r -> prefix.equals(r.getMetadata().getString("api.name")), newPath, method,
                                requestData);
    }

    private SingleSource<JsonObject> validateClientLevelMongoUser(MultiTenantUserProps userProps, JsonObject company) {
        return validateSitesIdsBody(userProps)
            .flatMap(sitesIds -> userProps.getMongoClient()
                .rxFind(SITE, new JsonObject().put("_id", new JsonObject().put("$in", sitesIds)))
                .flatMap(sites -> validateSitesAssociation(userProps, company, sitesIds, sites))
                .flatMap(ignored -> {
                    if (hasUserLevelRole(userProps.getBodyRole())) {
                        return validateUserLevelMongoUser(userProps, company);
                    } else {
                        JsonObject body = userProps.getBody();
                        body.put("group_id", "")
                            .put("associated_company_id", company.getString("associated_company_id"));
                        return Single.just(body);
                    }
                }));
    }

    private SingleSource<JsonObject> validateUserLevelMongoUser(MultiTenantUserProps userProps, JsonObject company) {
        String groupId = userProps.getBody().getString("group_id");
        if (groupId == null) {
            throw HttpException.badRequest("You must include group_id on the request data.");
        }

        return userProps.getMongoClient().rxFind(USER_GROUP, new JsonObject().put("site_id", userProps.getBodySiteId()))
            .flatMap(userGroups -> {
                if (userGroups != null) {
                    if (MongoUtils.getIdsOnList(userGroups).contains(groupId)) {
                        // For USER and GUEST company_id and associated_company_id be same
                        JsonObject body = userProps.getBody();
                        body.put("group_id", groupId).put("associated_company_id", company.getString("_id"));
                        return Single.just(body);
                    } else {
                        throw HttpException.badRequest("<UserGroup> doesn't exist on that <Site>.");
                    }
                } else {
                    throw HttpException.badRequest("<Site> doesn't have any <UserGroup>");
                }
            });
    }

    private SingleSource<Boolean> validateSitesAssociation(MultiTenantUserProps userProps,
                                                           JsonObject company, JsonArray sitesIds,
                                                           List<JsonObject> sites) {
        String siteId = userProps.getBody().getString("site_id", "");
        if (sites.size() != 0 && sites.size() == sitesIds.size()) {
            boolean isSiteAssociated = true;
            for (JsonObject site : sites) {
                if (!site.getString("associated_company_id").equals(company.getString("_id"))) {
                    isSiteAssociated = false;
                    break;
                }
            }
            if (isSiteAssociated && sitesIds.contains(siteId)) {
                return Single.just(true);
            } else {
                throw HttpException.forbidden();
            }
        } else {
            throw HttpException.badRequest("Site doesn't exist.");
        }
    }

    private Single<JsonArray> validateSitesIdsBody(MultiTenantUserProps userProps) {
        JsonArray sitesIds = userProps.getBody().getJsonArray("sites_ids", new JsonArray());
        if (sitesIds.size() == 0 && Strings.isNotBlank(userProps.getBody().getString("site_id"))) {
            return Single.just(new JsonArray().add(userProps.getBody().getString("site_id")));
        } else if (sitesIds.size() == 0) {
            throw HttpException.badRequest("You must include valid sites_ids on the request data.");
        }
        return Single.just(sitesIds);
    }

    private Future<ResponseData> handleGetUsers(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        Role role = getRole(user);
        String companyId = getCompanyId(user);

        if (role == Role.SUPER_ADMIN) {
            JsonObject eqToSupperAdmin = new JsonObject().put("$eq", Role.SUPER_ADMIN.toString());
            JsonObject getUserQuery = new JsonObject().put("role", new JsonObject().put("$not", eqToSupperAdmin));
            userRepresentation(mongoClient, getUserQuery, future);
        } else if (role == Role.ADMIN) {
            JsonObject getChildManagerCompanyQuery =
                new JsonObject().put("associated_company_id", companyId).put("role", Role.MANAGER.toString());
            mongoClient
                .rxFind(COMPANY, getChildManagerCompanyQuery)
                .subscribe(companies -> {
                    JsonArray accessibleCompany = MongoUtils.getIdsOnJsonArray(companies).add(companyId);
                    JsonObject getUserQuery = new JsonObject()
                        .put("associated_company_id", new JsonObject().put("$in", accessibleCompany));
                    userRepresentation(mongoClient, getUserQuery, future);
                }, throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        } else if (role == Role.MANAGER) {
            userRepresentation(mongoClient, new JsonObject().put("associated_company_id", companyId), future);
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

}
