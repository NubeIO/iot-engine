package io.nubespark.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.Role;
import io.nubespark.impl.models.*;
import io.nubespark.utils.*;
import io.nubespark.vertx.common.RxRestAPIVerticle;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.ext.mongo.MongoClient;

import java.util.List;
import java.util.stream.Collectors;

import static io.nubespark.constants.Address.MULTI_TENANT_ADDRESS;
import static io.nubespark.constants.Collection.*;
import static io.nubespark.utils.CustomMessageResponseHelper.*;
import static io.nubespark.utils.MongoUtils.idQuery;
import static io.nubespark.vertx.common.HttpHelper.badRequest;
import static io.nubespark.vertx.common.HttpHelper.forbidden;

public class MultiTenantVerticle extends RxRestAPIVerticle {
    private MongoClient mongoClient;
    private static final String DEFAULT_PASSWORD = "helloworld";
    private Logger logger = LoggerFactory.getLogger(MultiTenantVerticle.class);

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void start() {
        super.start();
        mongoClient = MongoClient.createNonShared(vertx, config().getJsonObject("mongo").getJsonObject("config"));
        EventBus eventBus = getVertx().eventBus();

        // Receive message
        eventBus.consumer(MULTI_TENANT_ADDRESS, this::handleRequest);
    }

    private void handleRequest(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        String url = customMessage.getHeader().getString("url");
        String method = customMessage.getHeader().getString("method");

        switch (method.toUpperCase()) {
            case "GET":
                switch (url) {
                    case "companies":
                        handleGetCompanies(message);
                        break;
                    case "users":
                        handleGetUsers(message);
                        break;
                    case "sites":
                        handleGetSites(message);
                        break;
                    case "user_groups":
                        handleGetUserGroups(message);
                        break;
                    default:
                        handleNotFoundResponse(message);
                        break;
                }
                break;
            case "POST":
                switch (url) {
                    case "user":
                        handlePostUser(message);
                        break;
                    case "company":
                        handlePostCompany(message);
                        break;
                    case "site":
                        handlePostSite(message);
                        break;
                    case "user_group":
                        handlePostUserGroup(message);
                        break;


                    case "delete_users":
                        handleDeleteUsers(message);
                        break;
                    case "delete_companies":
                        handleDeleteCompanies(message);
                        break;
                    case "delete_sites":
                        handleDeleteSites(message);
                        break;
                    case "delete_user_groups":
                        handleDeleteUserGroups(message);
                        break;
                    case "check_user":
                        handleCheckUser(message);
                        break;
                    default:
                        handleNotFoundResponse(message);
                        break;
                }
                break;
            case "PATCH":
                switch (url.split("/")[0]) {
                    case "password":
                        handleUpdatePassword(message);
                        break;
                    case "user":
                        handleUpdateUser(message);
                        break;
                    case "site":
                        handleUpdateSite(message);
                        break;
                    case "user_group":
                        handleUpdateUserGroup(message);
                        break;
                    default:
                        handleNotFoundResponse(message);
                        break;
                }
                break;
            case "PUT":
                switch (url) {
                    case "site":
                        handlePutSite(message);
                        break;
                    default:
                        handleNotFoundResponse(message);
                        break;
                }
                break;
            default:
                handleNotFoundResponse(message);
                break;
        }
    }

    private void handlePostUser(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonObject body = CustomMessageHelper.getBodyAsJson(message);
            KeycloakUserRepresentation userRepresentation = new KeycloakUserRepresentation(body);
            String accessToken = user.getString("access_token");
            JsonObject keycloakConfig = CustomMessageHelper.getKeycloakConfig(message);
            String authServerUrl = keycloakConfig.getString("auth-server-url");
            String realmName = keycloakConfig.getString("realm");

            HttpClient client = vertx.createHttpClient();

            // 1. Create User on Keycloak
            UserUtils.createUser(userRepresentation, accessToken, authServerUrl, realmName, client)
                // 2. GET recently created user details from Keycloak
                .flatMap(ignored -> UserUtils.getUserFromUsername(body.getString("username"), accessToken, authServerUrl, realmName, client))
                // 3. Resetting password; by default password: '<DEFAULT_PASSWORD>'
                .flatMap(keycloakUser -> UserUtils.resetPassword(keycloakUser.getString("id"), body.getString("password", DEFAULT_PASSWORD), accessToken, authServerUrl, realmName, client)
                    .flatMap(ignored -> {
                        if (role == Role.SUPER_ADMIN) {
                            // 4.1 any user can be created
                            return createMongoUser(user, body, accessToken, authServerUrl, realmName, client, keycloakUser, new JsonObject().put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))));
                        } else if (role == Role.ADMIN) {
                            // 4.2 only child companies can make associate with it's users
                            return byAdminCompanyGetAdminWithManagerSelectionListQuery(companyId)
                                .flatMap(query -> createMongoUser(user, body, accessToken, authServerUrl, realmName, client, keycloakUser, query));
                        } else {
                            // 4.3 Creating user on MongoDB with 'group_id'
                            return createMongoUserByManager(body, user, accessToken, client, authServerUrl, realmName, keycloakUser, user.getString("company_id"));
                        }
                    })).subscribe(statusCode -> message.reply(new CustomMessage<>(null, new JsonObject(), statusCode)), throwable -> handleHttpException(message, throwable));
        }
    }

    private SingleSource<? extends Integer> createMongoUser(JsonObject user, JsonObject body, String accessToken, String authServerUrl, String realmName, HttpClient client, JsonObject keycloakUser, JsonObject query) {
        return mongoClient.rxFind(USER, query)
            .flatMap(childCompanies -> {
                if (childCompanies.size() > 0) {
                    // 5.1 Proceed for creating MongoDB user
                    String[] childCompaniesIds = StringUtils.getIds(childCompanies);
                    String companyId = SQLUtils.getMatchValueOrDefaultOne(body.getString("company_id", ""), childCompaniesIds);
                    JsonObject companyJsonObject = JSONUtils.getMatchValueOrDefaultOne(childCompanies, companyId);

                    body.put("company_id", companyJsonObject.getString("_id"));
                    if (companyJsonObject.getString("role").equals(Role.MANAGER.toString()) && SQLUtils.in(body.getString("role", ""), Role.USER.toString(), Role.GUEST.toString())) {
                        return createMongoUserByManager(body, user, accessToken, client, authServerUrl, realmName, keycloakUser, companyJsonObject.getString("_id"));
                    } else {
                        body.put("associated_company_id", companyJsonObject.getString("associated_company_id"))
                            .put("role", companyJsonObject.getString("role"))
                            .put("site_id", "")
                            .put("group_id", "");

                        MongoUser mongoUser = new MongoUser(body, user, keycloakUser);
                        return mongoClient.rxSave(USER, mongoUser.toJsonObject())
                            .map(ignore -> HttpResponseStatus.CREATED.code());
                    }
                } else {
                    // 5.2 Remove user from Keycloak
                    return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName, client)
                        .map(ign -> {
                            throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <Company> at first.");
                        });
                }
            });
    }

    private SingleSource<? extends Integer> createMongoUserByManager(JsonObject body, JsonObject user, String accessToken, HttpClient client, String authServerUrl,
                                                                     String realmName, JsonObject keycloakUser, String companyId) {
        JsonObject query = new JsonObject().put("associated_company_id", companyId);
        return mongoClient.rxFind(SITE, query)
            .flatMap(childSites -> {
                if (childSites.size() > 0) {
                    return mongoClient.rxFind(USER_GROUP, query)
                        .flatMap(childGroups -> {
                            if (childGroups.size() > 0) {
                                // 5.1 Creating user on MongoDB
                                body.put("company_id", companyId)
                                    .put("associated_company_id", companyId)
                                    .put("site_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("site_id", ""), StringUtils.getIds(childSites)))
                                    .put("group_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("group_id", ""), StringUtils.getIds(childGroups)));
                                MongoUser mongoUser = new MongoUser(body, user, keycloakUser);
                                return mongoClient.rxSave(USER, mongoUser.toJsonObject())
                                    .map(ignore -> HttpResponseStatus.CREATED.code());
                            } else {
                                // 5.2 Remove user from Keycloak
                                return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName, client)
                                    .map(ign -> {
                                        throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <User Group> at first.");
                                    });
                            }
                        });
                } else {
                    // 5.2 Remove user from Keycloak
                    return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName, client)
                        .map(ign -> {
                            throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <Site> at first.");
                        });
                }
            });
    }

    private SingleSource<? extends Integer> updateOwnUser(JsonObject body, JsonObject ctxUser, JsonObject keycloakUser) {
        // User doesn't have the authority to update own company_id, associated_company_id, and group_id
        body.put("company_id", ctxUser.getString("company_id"))
            .put("associated_company_id", ctxUser.getString("associated_company_id"))
            .put("group_id", ctxUser.getString("group_id"));
        MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
        JsonObject mongoUserObject = mongoUser.toJsonObject().put("role", ctxUser.getString("role")); // Role shouldn't be overridden
        return mongoClient.rxSave(USER, mongoUserObject)
            .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
    }

    private void handlePostCompany(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);

        if (role == Role.SUPER_ADMIN) {
            JsonObject body = CustomMessageHelper.getBodyAsJson(message);
            String associatedCompanyId = body.getString("associated_company_id", "");
            if (StringUtils.isNotNull(associatedCompanyId)) {
                mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                    .flatMap(companyResponse -> {
                        if (StringUtils.isNull(companyResponse.toString())) {
                            throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Failed to get the associated_company");
                        } else {
                            Company company = new Company(body.put("role", UserUtils.getRole(Role.valueOf(companyResponse.getString("role")))));
                            return MongoUtils.postDocument(mongoClient, COMPANY, company.toJsonObject());
                        }
                    }).subscribe(ignore -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.OK.code())), throwable -> handleHttpException(message, throwable));
            } else {
                Company company = new Company(CustomMessageHelper.getBodyAsJson(message)
                    .put("associated_company_id", CustomMessageHelper.getAssociatedCompanyId(user))
                    .put("role", Role.ADMIN.toString()));
                MongoUtils.postDocument(mongoClient, COMPANY, company.toJsonObject())
                    .subscribe(
                        ignore -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.OK.code())),
                        throwable -> handleHttpException(message, throwable));
            }
        } else if (role == Role.ADMIN) {
            Company company = new Company(CustomMessageHelper.getBodyAsJson(message)
                .put("associated_company_id", CustomMessageHelper.getAssociatedCompanyId(user))
                .put("role", Role.MANAGER.toString()));

            MongoUtils.postDocument(mongoClient, COMPANY, company.toJsonObject())
                .subscribe(
                    ignore -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.OK.code())),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePostSite(Message<Object> message) {
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        if (role == Role.SUPER_ADMIN || role == Role.ADMIN) {
            String associatedCompanyId = body.getString("associated_company_id");
            mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                .flatMap(associatedCompany -> {
                    if (StringUtils.isNotNull(associatedCompany.toString())) {
                        if (associatedCompany.getString("role").equals(Role.MANAGER.toString())) {
                            if (role == Role.SUPER_ADMIN) {
                                return MongoUtils.postDocument(mongoClient, SITE,
                                    new Site(body.put("associated_company_id", associatedCompany.getString("_id")).put("role", Role.USER.toString())).toJsonObject());
                            } else {
                                return byAdminCompanyGetAdminWithManagerSelectionList(companyId)
                                    .flatMap(companies -> {
                                        if (companies.contains(associatedCompany.getString("_id"))) {
                                            return MongoUtils.postDocument(mongoClient, SITE,
                                                new Site(body.put("associated_company_id", associatedCompany.getString("_id")).put("role", Role.USER.toString())).toJsonObject());
                                        } else {
                                            throw forbidden();
                                        }
                                    });
                            }
                        } else {
                            throw forbidden();
                        }
                    } else {
                        throw badRequest("Failed to get the associated_company");
                    }
                })
                .subscribe(
                    ignore -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.CREATED.code())),
                    throwable -> handleHttpException(message, throwable));
        } else if (role == Role.MANAGER) {
            MongoUtils.postDocument(mongoClient, SITE,
                new Site(body.put("associated_company_id", companyId).put("role", Role.USER.toString())).toJsonObject())
                .subscribe(
                    ignore -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.CREATED.code())),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePostUserGroup(Message<Object> message) {
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String userCompanyId = CustomMessageHelper.getCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            getManagerSiteQuery(role, userCompanyId)
                .flatMap(managerSiteQuery -> mongoClient.rxFind(SITE, managerSiteQuery)
                    .flatMap(childCompaniesResponse -> {
                        if (childCompaniesResponse.size() > 0) {
                            String[] availableSites = StringUtils.getIds(childCompaniesResponse);
                            String site_id = SQLUtils.getMatchValueOrDefaultOne(CustomMessageHelper.getBodyAsJson(message).getString("site_id", ""), availableSites);
                            if (role == Role.MANAGER) {
                                return Single.just(new UserGroup(body
                                    .put("associated_company_id", userCompanyId)
                                    .put("role", Role.USER.toString())
                                    .put("site_id", site_id)));
                            } else {
                                String associatedCompanyId = body.getString("associated_company_id", "");
                                if (StringUtils.isNotNull(associatedCompanyId)) {
                                    return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                                        .map(response -> {
                                            if (StringUtils.isNotNull(response.toString())) {
                                                if (response.getString("role").equals(Role.MANAGER.toString())
                                                    && (role == Role.SUPER_ADMIN
                                                    || (role == Role.ADMIN && managerSiteQuery.getJsonObject("associated_company_id").getJsonArray("$in").contains(associatedCompanyId))
                                                )) {
                                                    return new UserGroup(body
                                                        .put("associated_company_id", associatedCompanyId)
                                                        .put("role", Role.USER.toString())
                                                        .put("site_id", site_id));
                                                } else {
                                                    throw forbidden();
                                                }
                                            } else {
                                                throw badRequest("We don't have the associated_company_id");
                                            }
                                        });
                                } else {
                                    throw badRequest("No associated_company_id value is requested.");
                                }
                            }
                        } else {
                            throw badRequest("Create <Site> at first.");
                        }
                    }))
                .flatMap(userGroup -> MongoUtils.postDocument(mongoClient, USER_GROUP, userGroup.toJsonObject()))
                .subscribe(
                    ignore -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.CREATED.code())),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleGetCompanies(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        Single.just(new JsonObject())
            .flatMap(ignore -> {
                if (role == Role.SUPER_ADMIN) {
                    return mongoClient.rxFind(COMPANY, new JsonObject().put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))));
                } else if (role == Role.ADMIN) {
                    return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId));
                } else {
                    throw forbidden();
                }
            })
            .flatMap(response -> Observable.fromIterable(response)
                .flatMapSingle(res -> {
                    JsonObject object = new JsonObject(res.toString());
                    String associatedCompanyId = object.getString("associated_company_id");
                    return associatedCompanyRepresentation(object, associatedCompanyId);
                }).toList()
            )
            .subscribe(response -> {
                JsonArray array = new JsonArray();
                response.forEach(jsonObject -> array.add(buildSiteWithAbsoluteImageUri(message, jsonObject)));
                message.reply(new CustomMessage<>(null, array, HttpResponseStatus.OK.code()));
            }, throwable -> handleHttpException(message, throwable));
    }

    private void handleGetUsers(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithCompanyAssociateCompanyGroupAndSiteRepresentation(message, new JsonObject().put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))), USER);
        } else if (role == Role.ADMIN) {
            // Returning all <Users> which is branches from the ADMIN
            mongoClient
                .rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId).put("role", Role.MANAGER.toString()))
                .subscribe(companies -> respondRequestWithCompanyAssociateCompanyGroupAndSiteRepresentation(message, new JsonObject()
                        .put("associated_company_id", new JsonObject()
                            .put("$in", StringUtils.getIdsJsonArray(companies).add(companyId))), USER),
                    throwable -> handleHttpException(message, throwable));
        } else if (role == Role.MANAGER) {
            respondRequestWithCompanyAssociateCompanyGroupAndSiteRepresentation(message, new JsonObject().put("associated_company_id", companyId), USER);
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleGetSites(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        Single.just(new JsonObject())
            .flatMap(ignored -> {
                if (role == Role.SUPER_ADMIN) {
                    return mongoClient.rxFind(SITE, new JsonObject().put("role", new JsonObject().put("$eq", Role.USER.toString())));
                } else if (role == Role.ADMIN) {
                    return byAdminCompanyGetManagerSelectionListQuery(companyId)
                        .flatMap(query -> mongoClient.rxFind(SITE, query));
                } else if (role == Role.MANAGER) {
                    return mongoClient.rxFind(SITE, new JsonObject().put("associated_company_id", companyId));
                } else {
                    throw forbidden();
                }
            })
            .flatMap(sites -> Observable.fromIterable(sites)
                .flatMapSingle(res -> {
                    JsonObject object = new JsonObject(res.toString());
                    String associatedCompanyId = object.getString("associated_company_id");
                    return associatedCompanyRepresentation(object, associatedCompanyId);
                }).toList())
            .subscribe(response -> {
                JsonArray array = new JsonArray();
                response.forEach(jsonObject -> array.add(buildSiteWithAbsoluteImageUri(message, jsonObject)));
                message.reply(new CustomMessage<>(null, array, HttpResponseStatus.OK.code()));
            }, throwable -> handleHttpException(message, throwable));
    }

    private void handleGetUserGroups(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        Single.just(new JsonObject())
            .flatMap(ignore -> {
                if (role == Role.SUPER_ADMIN) {
                    return mongoClient.rxFind(USER_GROUP, new JsonObject());
                } else if (role == Role.ADMIN) {
                    return byAdminCompanyGetManagerSelectionListQuery(companyId)
                        .flatMap(query -> mongoClient.rxFind(USER_GROUP, query));
                } else if (role == Role.MANAGER) {
                    return mongoClient.rxFind(USER_GROUP, new JsonObject().put("associated_company_id", companyId));
                } else {
                    throw forbidden();
                }
            })
            .flatMap(userGroups -> Observable.fromIterable(userGroups)
                .flatMapSingle(object -> mongoClient.rxFindOne(USER_GROUP, idQuery(object.getString("site_id")), null)
                    .flatMap(site -> {
                        if (StringUtils.isNotNull(site.toString())) {
                            object.put("site", buildSiteWithAbsoluteImageUri(message, site));
                        }
                        String associatedCompanyId = object.getString("associated_company_id");
                        return associatedCompanyRepresentation(object, associatedCompanyId);
                    })).toList()
            )
            .subscribe(response -> {
                JsonArray array = new JsonArray();
                response.forEach(array::add);
                message.reply(new CustomMessage<>(null, array, HttpResponseStatus.OK.code()));
            }, throwable -> handleHttpException(message, throwable));
    }

    private void handleDeleteUsers(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        // Model level permission; this is limited to SUPER_ADMIN, ADMIN and MANAGER
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonArray queryInput = CustomMessageHelper.getBodyAsJsonArray(message);
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));

            mongoClient.rxFind(USER, query)
                .flatMap(users -> {
                    if (users.size() == queryInput.size()) {
                        return checkPermissionAndReturnValue(role, companyId, users);
                    }
                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Doesn't have those Users on Database.");
                })
                .flatMap(users -> Observable.fromIterable(users)
                    .flatMapSingle(userObject -> deleteUserFromKeycloakAndMongo(message, userObject))
                    .toList())
                .subscribe(ignored -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())), throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private SingleSource<? extends Integer> deleteUserFromKeycloakAndMongo(Message<Object> message, Object userObject) {
        JsonObject userObjectJson = (JsonObject) (userObject);
        JsonObject user = CustomMessageHelper.getUser(message);
        String accessToken = CustomMessageHelper.getAccessToken(user);
        JsonObject keycloakConfig = CustomMessageHelper.getKeycloakConfig(message);

        HttpClient client = vertx.createHttpClient();

        return UserUtils.deleteUser(userObjectJson.getString("_id"),
            accessToken,
            keycloakConfig.getString("auth-server-url"),
            keycloakConfig.getString("realm"),
            client)
            .flatMap(deleteUserKeycloakResponse -> {
                if (deleteUserKeycloakResponse.getInteger("statusCode") == HttpResponseStatus.NO_CONTENT.code()) {
                    JsonObject queryToDeleteOne = new JsonObject().put("_id", new JsonObject()
                        .put("$in", new JsonArray().add(userObjectJson.getString("_id"))));

                    return mongoClient.rxRemoveDocuments(USER, queryToDeleteOne)
                        .map(deleteUserResponse -> HttpResponseStatus.NO_CONTENT.code());
                } else {
                    throw new HttpException(deleteUserKeycloakResponse.getInteger("statusCode"), "Users are unable to deleted from the services.");
                }
            });
    }

    private void handleDeleteCompanies(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        // Model level permission; this is limited to SUPER_ADMIN and ADMIN
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            JsonArray queryInput = CustomMessageHelper.getBodyAsJsonArray(message);
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            mongoClient.rxFind(COMPANY, query)
                .flatMap(companies -> {
                    if (companies.size() == queryInput.size()) {
                        return checkPermissionAndReturnValue(role, companyId, companies);
                    } else {
                        throw badRequest("Doesn't have those <Companies> on Database.");
                    }
                })
                .flatMap(companies -> mongoClient.rxRemoveDocuments(COMPANY, query))
                .subscribe(ignore ->
                        message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private SingleSource<? extends List<JsonObject>> checkPermissionAndReturnValue(Role role, String companyId, List<JsonObject> objects) {
        return Observable.fromIterable(objects)
            .flatMapSingle(jsonObject -> objectLevelPermission(role, jsonObject.getString("associated_company_id"), companyId)
                .map(permitted -> {
                    if (!permitted) {
                        throw new HttpException(HttpResponseStatus.FORBIDDEN, "You don't have permission to perform the action.");
                    }
                    return jsonObject;
                })).toList();
    }

    private void handleDeleteSites(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        // Model level permission
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonArray queryInput = CustomMessageHelper.getBodyAsJsonArray(message);
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            mongoClient.rxFind(SITE, query)
                .flatMap(sites -> {
                    if (sites.size() == queryInput.size()) {
                        return checkPermissionAndReturnValue(role, companyId, sites);
                    } else {
                        throw badRequest("Doesn't have those <Sites> on Database.");
                    }
                })
                .flatMap(ignore -> mongoClient.rxRemoveDocuments(SITE, query))
                .subscribe(ignored ->
                        message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleDeleteUserGroups(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        // Model level permission
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonArray queryInput = CustomMessageHelper.getBodyAsJsonArray(message);
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            mongoClient.rxFind(USER_GROUP, query)
                .flatMap(userGroups -> {
                    if (userGroups.size() == queryInput.size()) {
                        return checkPermissionAndReturnValue(role, companyId, userGroups);
                    } else {
                        throw badRequest("Doesn't have those <User Groups> on Database.");
                    }
                })
                .flatMap(ignored -> mongoClient.rxRemoveDocuments(USER_GROUP, query))
                .subscribe(ignored -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())), throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleCheckUser(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        String username = body.getString("username", "");
        String email = body.getString("email", "");
        String query = "username=" + username + "&email=" + email;
        String accessToken = CustomMessageHelper.getAccessToken(user);
        JsonObject keycloakConfig = CustomMessageHelper.getKeycloakConfig(message);
        String authServerUrl = keycloakConfig.getString("auth-server-url");
        String realmName = keycloakConfig.getString("realm");
        HttpClient client = vertx.createHttpClient();

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
                    message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.FOUND.code()));
                } else {
                    message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NOT_FOUND.code()));
                }
            }, throwable -> handleHttpException(message, throwable));
    }

    private void handleUpdatePassword(Message<Object> message) {
        JsonObject ctxUser = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(ctxUser);
        String userId = CustomMessageHelper.getParamsId(message);
        String password = CustomMessageHelper.getBodyAsJson(message).getString("password", "");
        String accessToken = CustomMessageHelper.getAccessToken(ctxUser);
        JsonObject keycloakConfig = CustomMessageHelper.getKeycloakConfig(message);
        String authServerUrl = keycloakConfig.getString("auth-server-url");
        String realmName = keycloakConfig.getString("realm");

        HttpClient client = vertx.createHttpClient();

        if (StringUtils.isNotNull(password)) {
            mongoClient.rxFindOne(USER, idQuery(userId), null)
                .map(response -> {
                    if (StringUtils.isNull(response.toString())) {
                        throw new HttpException(HttpResponseStatus.BAD_REQUEST);
                    } else {
                        return response;
                    }
                })
                .flatMap(user -> {
                    // Own password can be changed or those users passwords which is associated with some company
                    if (ctxUser.getString("user_id").equals(userId)) {
                        return UserUtils.resetPassword(userId, password, accessToken, authServerUrl, realmName, client);
                    } else {
                        return objectLevelPermission(role, user.getString("associated_company_id"), ctxUser.getString("company_id"))
                            .map(permitted -> {
                                if (permitted) {
                                    return UserUtils.resetPassword(userId, password, accessToken, authServerUrl, realmName, client);
                                } else {
                                    throw forbidden();
                                }
                            });
                    }
                }).subscribe(ignored -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())), throwable -> handleHttpException(message, throwable));
        } else {
            message.reply(new CustomMessage<>(null, new JsonObject().put("message", "Password can't be NULL."), HttpResponseStatus.BAD_REQUEST.code()));
        }
    }

    private void handleUpdateUser(Message<Object> message) {
        JsonObject ctxUser = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(ctxUser);
        String userId = CustomMessageHelper.getParamsId(message);
        String accessToken = CustomMessageHelper.getAccessToken(ctxUser);
        JsonObject keycloakConfig = CustomMessageHelper.getKeycloakConfig(message);
        String authServerUrl = keycloakConfig.getString("auth-server-url");
        String realmName = keycloakConfig.getString("realm");
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        KeycloakUserRepresentation keycloakUserRepresentation = new KeycloakUserRepresentation(body);

        HttpClient client = vertx.createHttpClient();

        mongoClient.rxFindOne(USER, idQuery(userId), null)
            .map(response -> {
                if (StringUtils.isNull(response.toString())) {
                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Invalid user_id.");
                } else {
                    return response;
                }
            })
            .flatMap(user -> {
                logger.info("Responded user: " + user);
                // Own user_profile can be changed or those users_profiles which is associated with same company
                if (ctxUser.getString("user_id").equals(userId)
                    || (role == Role.MANAGER) && ctxUser.getString("company_id").equals(CustomMessageHelper.getAssociatedCompanyId(user))
                    || role.toString().equals(Role.SUPER_ADMIN.toString())) {

                    return UserUtils.updateUser(userId, keycloakUserRepresentation, accessToken, authServerUrl, realmName, client);
                } else if (role == Role.ADMIN) {
                    return byAdminCompanyGetAdminWithManagerSelectionList(CustomMessageHelper.getCompanyId(user))
                        .flatMap(response -> {
                            if (SQLUtils.inList(CustomMessageHelper.getCompanyId(user), response)) {
                                return UserUtils.updateUser(userId, keycloakUserRepresentation, accessToken, authServerUrl, realmName, client);
                            } else {
                                throw forbidden();
                            }
                        });
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
                    if (role == Role.SUPER_ADMIN) {
                        // Only child <Companies> can be added by the parent
                        return updateMongoUser(ctxUser, role, body, keycloakUser, new JsonObject().put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))));
                    } else if (role == Role.ADMIN) {
                        return byAdminCompanyGetAdminWithManagerSelectionListQuery(CustomMessageHelper.getCompanyId(ctxUser))
                            .flatMap(query -> updateMongoUser(ctxUser, role, body, keycloakUser, query));
                    } else {
                        // Only child <User Groups> can be added by the parent
                        return updateMongoUserByManager(ctxUser, body, keycloakUser, ctxUser.getString("company_id"));
                    }
                } else {
                    return updateOwnUser(body, ctxUser, keycloakUser);
                }
            }).subscribe(statusCode -> message.reply(new CustomMessage<>(null, new JsonObject(), statusCode)), throwable -> handleHttpException(message, throwable));
    }

    private SingleSource<? extends Integer> updateMongoUser(JsonObject ctxUser, Role role, JsonObject body, JsonObject keycloakUser, JsonObject query) {
        return mongoClient.rxFind(COMPANY, query)
            .flatMap(childCompanies -> {
                if (childCompanies.size() > 0) {
                    String[] _ids = StringUtils.getIds(childCompanies);
                    String companyId = SQLUtils.getMatchValueOrDefaultOne(body.getString("company_id", ""), _ids);
                    JsonObject companyJsonObject = JSONUtils.getMatchValueOrDefaultOne(childCompanies, companyId);

                    body.put("company_id", companyJsonObject.getString("_id"));

                    if (companyJsonObject.getString("role").equals(Role.MANAGER.toString()) && SQLUtils.in(body.getString("role", ""), Role.USER.toString(), Role.GUEST.toString())) {
                        return updateMongoUserByManager(ctxUser, body, keycloakUser, companyJsonObject.getString("_id"));
                    } else {
                        body.put("associated_company_id", companyJsonObject.getString("associated_company_id"))
                            .put("role", companyJsonObject.getString("role"))
                            .put("site_id", "")
                            .put("group_id", "");

                        MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
                        return mongoClient.rxSave(USER, mongoUser.toJsonObject())
                            .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
                    }
                } else {
                    // This case shouldn't be happened; otherwise only half operation will be successful
                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <Company> at first.");
                }
            });
    }

    private SingleSource<? extends Integer> updateMongoUserByManager(JsonObject ctxUser, JsonObject body, JsonObject keycloakUser, String companyId) {
        JsonObject query = new JsonObject().put("associated_company_id", companyId);
        return mongoClient.rxFind(SITE, query)
            .flatMap(childSites -> {
                if (childSites.size() > 0) {
                    return mongoClient.rxFind(USER, query)
                        .flatMap(childGroups -> {
                            if (childGroups.size() > 0) {
                                body.put("company_id", companyId)
                                    .put("associated_company_id", companyId)
                                    .put("site_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("site_id", ""), StringUtils.getIds(childSites)))
                                    .put("group_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("group_id", ""), StringUtils.getIds(childGroups)));
                                MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
                                return mongoClient.rxSave(USER, mongoUser.toJsonObject())
                                    .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
                            } else {
                                throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <User Group> at first.");
                            }
                        });
                } else {
                    throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <Site> at first.");
                }
            });
    }

    private void handleUpdateSite(Message<Object> message) {
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            String siteId = CustomMessageHelper.getParamsId(message);
            mongoClient.rxFindOne(SITE, idQuery(siteId), null)
                .flatMap(site -> {
                    if (StringUtils.isNotNull(site.toString())) {
                        return objectLevelPermission(role, site.getString("associated_company_id"), companyId)
                            .map(permitted -> {
                                if (permitted) {
                                    return site;
                                } else {
                                    throw forbidden();
                                }
                            });
                    } else {
                        throw badRequest("Requested site doesn't exist on Database");
                    }
                })
                .flatMap(site -> {
                    if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                        String associatedCompanyId = body.getString("associated_company_id");
                        return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                            .flatMap(associatedCompany -> {
                                if (StringUtils.isNotNull(associatedCompany.toString())) {
                                    if (associatedCompany.getString("role").equals(Role.MANAGER.toString())) {
                                        if (role == Role.SUPER_ADMIN) {
                                            return updateSite(body, role, associatedCompany.getString("_id"), site);
                                        } else {
                                            return byAdminCompanyGetAdminWithManagerSelectionList(companyId)
                                                .flatMap(companies -> {
                                                    if (companies.contains(associatedCompany.getString("_id"))) {
                                                        return updateSite(body, role, associatedCompany.getString("_id"), site);
                                                    } else {
                                                        throw forbidden();
                                                    }
                                                });
                                        }
                                    } else {
                                        throw forbidden();
                                    }
                                } else {
                                    throw badRequest("Requested company doesn't exist on Database.");
                                }
                            });
                    } else {
                        return updateSite(body, role, companyId, site);
                    }
                })
                .subscribe(ignored -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private SingleSource<?> updateSite(JsonObject body, Role role, String companyId, JsonObject site) {
        JsonObject siteObject = new Site(body
            .put("associated_company_id", companyId)).toJsonObject()
            .put("role", UserUtils.getRole(role).toString())
            .put("_id", site.getString("_id"));
        return mongoClient.rxSave(SITE, siteObject);
    }

    private void handleUpdateUserGroup(Message<Object> message) {
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String userCompanyId = CustomMessageHelper.getCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            String userGroupId = CustomMessageHelper.getParamsId(message);

            mongoClient.rxFindOne(USER_GROUP, idQuery(userGroupId), null)
                .flatMap(userObject -> objectLevelPermission(role, userObject.getString("associated_company_id"), userCompanyId)
                    .map(permitted -> {
                        if (permitted) {
                            return userObject;
                        } else {
                            throw forbidden();
                        }
                    }))
                .flatMap(userGroup -> getManagerSiteQuery(role, userCompanyId)
                    .flatMap(managerSiteQuery -> mongoClient.rxFind(SITE, managerSiteQuery)
                        .flatMap(childCompaniesResponse -> {
                            if (childCompaniesResponse.size() > 0) {
                                String[] availableSites = StringUtils.getIds(childCompaniesResponse);
                                String siteId = SQLUtils.getMatchValueOrDefaultOne(body.getString("site_id", ""), availableSites);
                                if (role == Role.MANAGER) {
                                    return Single.just(new UserGroup(body
                                        .put("associated_company_id", userCompanyId)
                                        .put("role", Role.USER.toString())
                                        .put("site_id", siteId))
                                        .toJsonObject().put("_id", userGroup.getString("_id")));
                                } else {
                                    String associatedCompanyId = body.getString("associated_company_id", "");
                                    if (StringUtils.isNotNull(associatedCompanyId)) {
                                        return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                                            .map(company -> {
                                                if (StringUtils.isNotNull(company.toString())) {
                                                    if (company.getString("role").equals(Role.MANAGER.toString())
                                                        && (role == Role.SUPER_ADMIN
                                                        || (role == Role.ADMIN && managerSiteQuery.getJsonObject("associated_company_id").getJsonArray("$in").contains(associatedCompanyId))
                                                    )) {
                                                        return new UserGroup(body
                                                            .put("associated_company_id", associatedCompanyId)
                                                            .put("role", Role.USER.toString())
                                                            .put("site_id", siteId))
                                                            .toJsonObject().put("_id", userGroup.getString("_id"));
                                                    } else {
                                                        throw forbidden();
                                                    }
                                                } else {
                                                    throw badRequest("We don't have the associated_company_id.");
                                                }
                                            });
                                    } else {
                                        throw badRequest("No associated_company_id value is requested.");
                                    }
                                }
                            } else {
                                throw badRequest("Create <Site> at first.");
                            }
                        })))
                .flatMap(userGroupObject -> mongoClient.rxSave(USER_GROUP, userGroupObject))
                .subscribe(ignore -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())), throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePutSite(Message<Object> message) {
        // This will call on the first time site initialization and update site
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String associatedCompanyId = CustomMessageHelper.getAssociatedCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            JsonObject query = new JsonObject().put("associated_company_id", associatedCompanyId);
            mongoClient.rxFind(SITE, query)
                .flatMap(getSites -> {
                    Site site = new Site(CustomMessageHelper.getBodyAsJson(message)
                        .put("associated_company_id", associatedCompanyId)
                        .put("role", role.toString()));
                    if (getSites.size() > 0) {
                        return mongoClient.rxSave(SITE, site.toJsonObject().put("_id", getSites.get(0).getString("_id")));
                    } else {
                        return mongoClient.rxSave(SITE, site.toJsonObject())
                            .flatMap(siteResponse -> mongoClient.rxFind(SITE, query)
                                .flatMap(sites -> {
                                    String siteId = sites.get(0).getString("_id");
                                    return mongoClient.rxUpdateCollectionWithOptions(USER, query, new JsonObject().put("$set", new JsonObject().put("site_id", siteId)), new UpdateOptions(false, true));
                                }));
                    }
                })
                .subscribe(ignored ->
                        message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.OK.code())),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private SingleSource<? extends JsonObject> associatedCompanyRepresentation(JsonObject object, String associatedCompanyId) {
        if (StringUtils.isNotNull(associatedCompanyId)) {
            return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                .map(associatedCompany -> {
                    if (StringUtils.isNotNull(associatedCompany.toString())) {
                        return object.put("associated_company", associatedCompany);
                    }
                    return object;
                });
        } else {
            return Single.just(object);
        }
    }

    private void respondRequestWithCompanyAssociateCompanyGroupAndSiteRepresentation(Message<Object> message, JsonObject query, String collection) {
        // We may do optimize version of this
        mongoClient.rxFind(collection, query)
            .flatMap(response -> Observable.fromIterable(response)
                .flatMapSingle(res -> {
                    JsonObject object = new JsonObject(res.toString());
                    return mongoClient.rxFindOne(COMPANY, idQuery(object.getString("associated_company_id")), null)
                        .flatMap(associatedCompany -> {
                            if (StringUtils.isNotNull(associatedCompany.toString())) {
                                object.put("associated_company", associatedCompany);
                            }
                            return mongoClient.rxFindOne(COMPANY, idQuery(object.getString("company_id")), null)
                                .flatMap(company -> {
                                    if (StringUtils.isNotNull(company.toString())) {
                                        object.put("company", company);
                                    }
                                    if (StringUtils.isNotNull(object.getString("group_id"))) {
                                        return mongoClient.rxFindOne(USER_GROUP, idQuery(object.getString("group_id")), null)
                                            .flatMap(group -> {
                                                if (StringUtils.isNotNull(group.toString())) {
                                                    object.put("group", group);
                                                }
                                                return respondSiteWithAbsolutePath(message, object);
                                            });
                                    } else {
                                        return respondSiteWithAbsolutePath(message, object);
                                    }
                                });
                        });
                }).toList()
            ).subscribe(response -> {
                JsonArray array = new JsonArray();
                response.forEach(array::add);
                message.reply(new CustomMessage<>(null, array, HttpResponseStatus.OK.code()));
            },
            throwable -> handleHttpException(message, throwable));
    }

    private SingleSource<? extends JsonObject> respondSiteWithAbsolutePath(Message<Object> message, JsonObject object) {
        if (StringUtils.isNotNull(object.getString("site_id"))) {
            return mongoClient.rxFindOne(SITE, idQuery(object.getString("site_id")), null)
                .map(site -> {
                    if (StringUtils.isNotNull(site.toString())) {
                        object.put("site", buildSiteWithAbsoluteImageUri(message, site));
                    }
                    return object;
                });
        } else {
            return Single.just(object);
        }
    }

    private JsonObject buildSiteWithAbsoluteImageUri(Message<Object> message, JsonObject site) {
        if (StringUtils.isNotNull(site.getString("logo_sm")) || StringUtils.isNotNull(site.getString("logo_md"))) {
            return site
                .put("logo_sm", CustomMessageHelper.buildAbsoluteUri(message, site.getString("logo_sm")))
                .put("logo_md", CustomMessageHelper.buildAbsoluteUri(message, site.getString("logo_md")));
        } else {
            return site;
        }
    }

    private Single<JsonObject> byAdminCompanyGetManagerSelectionListQuery(String companyId) {
        return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId).put("role", Role.MANAGER.toString()))
            .map(response -> new JsonObject().put("associated_company_id", new JsonObject().put("$in", StringUtils.getIdsJsonArray(response).add(companyId))));
    }

    private Single<JsonObject> byAdminCompanyGetAdminWithManagerSelectionListQuery(String companyId) {
        return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId).put("role", Role.MANAGER.toString()))
            .map(response -> new JsonObject().put("associated_company_id", new JsonObject().put("$in", StringUtils.getIdsJsonArray(response).add(companyId))));
    }

    private Single<List<String>> byAdminCompanyGetAdminWithManagerSelectionList(String companyId) {
        return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId).put("role", Role.MANAGER.toString()))
            .map(response -> {
                List<String> companies = StringUtils.getIdsList(response);
                companies.add(companyId);
                return companies;
            });
    }

    private Single<JsonObject> getManagerSiteQuery(Role role, String userCompanyId) {
        if (role == Role.SUPER_ADMIN) {
            return Single.just(new JsonObject().put("role", Role.USER));
        } else if (role == Role.ADMIN) {
            return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", userCompanyId).put("role", Role.MANAGER.toString()))
                .map(response -> new JsonObject().put("associated_company_id", new JsonObject().put("$in", StringUtils.getIdsJsonArray(response))));
        } else {
            return Single.just(new JsonObject().put("associated_company_id", userCompanyId));
        }
    }

    private Single<Boolean> objectLevelPermission(Role role, String toCheckCompanyId, String companyId) {
        if (role == Role.SUPER_ADMIN) {
            return Single.just(true);
        } else if (role == Role.ADMIN) {
            return byAdminCompanyGetAdminWithManagerSelectionList(companyId).map(list -> list.contains(toCheckCompanyId));
        } else if (role == Role.MANAGER) {
            return Single.just(companyId.equals(toCheckCompanyId));
        }
        return Single.just(false);
    }
}
