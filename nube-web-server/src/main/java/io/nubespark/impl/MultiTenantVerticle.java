package io.nubespark.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.Role;
import io.nubespark.controller.HttpException;
import io.nubespark.impl.models.*;
import io.nubespark.utils.*;
import io.nubespark.vertx.common.RxRestAPIVerticle;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.http.HttpClient;

import java.util.stream.Collectors;

import static io.nubespark.constants.Address.MULTI_TENANT_ADDRESS;
import static io.nubespark.utils.CustomMessageResponseHelper.*;
import static io.nubespark.vertx.common.HttpHelper.badRequest;
import static io.nubespark.vertx.common.HttpHelper.forbidden;

public class MultiTenantVerticle extends RxRestAPIVerticle {
    private static final String DEFAULT_PASSWORD = "helloworld";
    private Logger logger = LoggerFactory.getLogger(MultiTenantVerticle.class);

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void start() {
        super.start();
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
            default:
                handleNotFoundResponse(message);
                break;
        }
    }

    private void handlePostUser(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        String role = CustomMessageHelper.getRoleString(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        if (SQLUtils.in(role, Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
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
                        if (SQLUtils.in(role, Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                            // 4.1 only child companies can make associate with it's users
                            return dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject().put("associated_company_id", companyId))
                                .flatMap(response -> {
                                    JsonArray childCompanies = response.getDelegate().toJsonArray();
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
                    })).subscribe(statusCode -> message.reply(new CustomMessage<>(null, new JsonObject(), statusCode)), throwable -> handleHttpException(message, throwable));
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
                        // Role 'MANAGER' will assign it's own company_id as the company_id
                        body.put("company_id", user.getString("company_id"));
                    }

                    String[] _ids = StringUtils.getIds(childGroups);
                    body.put("associated_company_id", user.getString("company_id"))
                        .put("group_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("group_id", ""), _ids));
                    MongoUser mongoUser = new MongoUser(body, user, keycloakUser);
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


    private SingleSource<? extends Integer> updateMongoUser(JsonObject ctxUser, JsonObject body, JsonObject keycloakUser, JsonArray childCompanies) {
        return dispatchRequests(HttpMethod.POST, URL.get_user_group, new JsonObject().put("associated_company_id", ctxUser.getString("company_id")))
            .flatMap(response -> {
                JsonArray childGroups = new JsonArray(response.getDelegate());
                if (childGroups.size() > 0) {
                    if (childCompanies.size() > 0) {
                        String[] _ids = StringUtils.getIds(childCompanies);
                        body.put("company_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("company_id", ""), _ids));
                    } else {
                        // Role 'MANAGER' will assign it's own company_id as the company_id
                        body.put("company_id", ctxUser.getString("company_id"));
                    }
                    String[] _ids = StringUtils.getIds(childGroups);
                    body.put("associated_company_id", ctxUser.getString("company_id"))
                        .put("group_id", SQLUtils.getMatchValueOrDefaultOne(body.getString("group_id", ""), _ids));
                    MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
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
        return dispatchRequests(HttpMethod.PUT, URL.put_user, mongoUserObject)
            .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
    }

    private void handlePostCompany(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        String role = CustomMessageHelper.getRoleString(user);

        if (SQLUtils.in(role, Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            Company company = new Company(CustomMessageHelper.getBodyAsJson(message), user);
            dispatchRequests(HttpMethod.POST, URL.post_company, company.toJsonObject())
                .subscribe(
                    result -> message.reply(new CustomMessage<>(null, new JsonObject(), result.getDelegate().toJsonObject().getInteger("statusCode"))),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePostSite(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            Site site = new Site(CustomMessageHelper.getBodyAsJson(message)
                .put("associated_company_id", companyId)
                .put("role", UserUtils.getRole(role).toString()));
            dispatchRequests(HttpMethod.POST, URL.post_site, site.toJsonObject())
                .subscribe(
                    siteResponse -> message.reply(new CustomMessage<>(null, new JsonObject(), siteResponse.getDelegate().toJsonObject().getInteger("statusCode"))),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePostUserGroup(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            // Only manager's sites should make available for user_group
            dispatchRequests(HttpMethod.POST, URL.get_site, new JsonObject().put("associated_company_id", companyId))
                .map(buffer -> {
                    JsonArray childCompaniesResponse = new JsonArray(buffer.getDelegate());
                    if (childCompaniesResponse.size() > 0) {
                        String[] availableSites = StringUtils.getIds(childCompaniesResponse);
                        String site_id = SQLUtils.getMatchValueOrDefaultOne(CustomMessageHelper.getBodyAsJson(message).getString("site_id", ""), availableSites);
                        return new UserGroup(CustomMessageHelper.getBodyAsJson(message)
                            .put("associated_company_id", companyId)
                            .put("role", UserUtils.getRole(role).toString())
                            .put("site_id", site_id));
                    } else {
                        throw badRequest("Create <Site> at first.");
                    }
                })
                .flatMap(userGroup -> dispatchRequests(HttpMethod.POST, URL.post_user_group, userGroup.toJsonObject()))
                .subscribe(
                    buffer -> message.reply(new CustomMessage<>(null, new JsonObject(), buffer.getDelegate().toJsonObject().getInteger("statusCode"))),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleGetCompanies(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithAssociateCompanyRepresentation(message, new JsonObject().put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))), URL.get_company);
        } else if (role == Role.ADMIN) {
            respondRequestWithAssociateCompanyRepresentation(message, new JsonObject().put("associated_company_id", companyId), URL.get_company);
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleGetUsers(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithCompanyAssociateCompanyAndGroupRepresentation(message, new JsonObject().put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))), URL.get_user);
        } else if (role == Role.ADMIN) {
            // Returning all <Users> which is branches from the ADMIN
            dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject()
                .put("associated_company_id", companyId)
                .put("role", Role.MANAGER.toString()))
                .subscribe(buffer -> respondRequestWithCompanyAssociateCompanyAndGroupRepresentation(message, new JsonObject()
                        .put("associated_company_id", new JsonObject()
                            .put("$in", StringUtils.getIdsJsonArray(new JsonArray(buffer.getDelegate()))
                                .add(companyId))), URL.get_user),
                    throwable -> handleHttpException(message, throwable));
        } else if (role == Role.MANAGER) {
            respondRequestWithCompanyAssociateCompanyAndGroupRepresentation(message, new JsonObject().put("associated_company_id", companyId), URL.get_user);
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleGetSites(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithAssociateCompanyRepresentation(message, new JsonObject(), URL.get_site);
        } else if (role == Role.ADMIN) {
            // Returning all MANAGER's companies' <sites> which is associated with the ADMIN company
            dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject()
                .put("associated_company_id", companyId)
                .put("role", Role.MANAGER.toString()))
                .subscribe(
                    buffer -> respondRequestWithAssociateCompanyRepresentation(message, new JsonObject()
                        .put("associated_company_id", new JsonObject()
                            .put("$in", StringUtils.getIdsJsonArray(new JsonArray(buffer.getDelegate()))
                                .add(companyId))), URL.get_site),
                    throwable -> handleHttpException(message, throwable));
        } else if (role == Role.MANAGER) {
            respondRequestWithAssociateCompanyRepresentation(message, new JsonObject().put("associated_company_id", companyId), URL.get_site);
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleGetUserGroups(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithSiteAndAssociateCompanyRepresentation(message, new JsonObject(), URL.get_user_group);
        } else if (role == Role.ADMIN) {
            // Returning all MANAGER's companies' <user groups> which is associated with the ADMIN company
            dispatchRequests(HttpMethod.POST, URL.get_company, new JsonObject()
                .put("associated_company_id", companyId)
                .put("role", Role.MANAGER.toString()))
                .subscribe(
                    buffer -> respondRequestWithSiteAndAssociateCompanyRepresentation(message, new JsonObject()
                        .put("associated_company_id", new JsonObject()
                            .put("$in", StringUtils.getIdsJsonArray(new JsonArray(buffer.getDelegate()))
                                .add(companyId))), URL.get_user_group),
                    throwable -> handleHttpException(message, throwable));
        } else if (role == Role.MANAGER) {
            respondRequestWithSiteAndAssociateCompanyRepresentation(message, new JsonObject().put("associated_company_id", companyId), URL.get_user_group);
        } else {
            handleForbiddenResponse(message);
        }
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

            dispatchRequests(HttpMethod.POST, URL.get_user, query)
                .map(buffer -> {
                    JsonArray users = new JsonArray(buffer.getDelegate());
                    if (users.size() == queryInput.size()) {
                        for (Object userObject : users) {
                            JsonObject userObjectJson = (JsonObject) (userObject);
                            if (!userObjectJson.getString("associated_company_id").equals(companyId)) {
                                throw new HttpException(HttpResponseStatus.FORBIDDEN, "You don't have permission to perform the action.");
                            }
                        }
                        return users;
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

    private void handleDeleteCompanies(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        // Model level permission; this is limited to SUPER_ADMIN and ADMIN
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            JsonArray queryInput = CustomMessageHelper.getBodyAsJsonArray(message);
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            dispatchRequests(HttpMethod.POST, URL.get_company, query)
                .flatMap(buffer -> {
                    JsonArray companies = new JsonArray(buffer.getDelegate());
                    if (companies.size() == queryInput.size()) {
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
                }).subscribe(ignored -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())), throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
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
            dispatchRequests(HttpMethod.POST, URL.get_site, query)
                .flatMap(buffer -> {
                    JsonArray sites = new JsonArray(buffer.getDelegate());
                    if (sites.size() == queryInput.size()) {
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
                }).subscribe(ignored -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())), throwable -> handleHttpException(message, throwable));
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
            dispatchRequests(HttpMethod.POST, URL.get_user_group, query)
                .flatMap(buffer -> {
                    JsonArray userGroups = new JsonArray(buffer.getDelegate());
                    if (userGroups.size() == queryInput.size()) {
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
                }).subscribe(ignored -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())), throwable -> handleHttpException(message, throwable));
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
            }).subscribe(statusCode -> message.reply(new CustomMessage<>(null, new JsonObject(), statusCode)), throwable -> handleHttpException(message, throwable));
    }

    private void handleUpdateSite(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            String siteId = CustomMessageHelper.getParamsId(message);
            dispatchRequests(HttpMethod.GET, URL.get_site + "/" + siteId, new JsonObject())
                .map(buffer -> {
                    if (StringUtils.isNull(buffer.toString()) || !buffer.toJsonObject().getString("associated_company_id").equals(companyId)) {
                        throw forbidden();
                    } else {
                        return new JsonObject(buffer.getDelegate());
                    }
                })
                .flatMap(site -> {
                    JsonObject siteObject = new Site(CustomMessageHelper.getBodyAsJson(message)
                        .put("associated_company_id", companyId)).toJsonObject()
                        .put("role", UserUtils.getRole(role).toString())
                        .put("_id", site.getString("_id"));
                    return dispatchRequests(HttpMethod.PUT, URL.put_site, siteObject);
                })
                .subscribe(ignored -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                    throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleUpdateUserGroup(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            String userGroupId = CustomMessageHelper.getParamsId(message);

            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + userGroupId, new JsonObject())
                .map(buffer -> {
                    if (StringUtils.isNull(buffer.toString()) || !buffer.toJsonObject().getString("associated_company_id").equals(companyId)) {
                        throw forbidden();
                    } else {
                        return new JsonObject(buffer.getDelegate());
                    }
                })
                .flatMap(userGroup ->
                    dispatchRequests(HttpMethod.POST, URL.get_site, new JsonObject().put("associated_company_id", companyId))
                        .map(buffer -> {
                            JsonArray childCompaniesResponse = new JsonArray(buffer.getDelegate());
                            if (childCompaniesResponse.size() > 0) {
                                String[] availableSites = StringUtils.getIds(childCompaniesResponse);
                                String siteId = SQLUtils.getMatchValueOrDefaultOne(body.getString("site_id", ""), availableSites);
                                return new UserGroup(body
                                    .put("associated_company_id", companyId)
                                    .put("role", UserUtils.getRole(role).toString())
                                    .put("site_id", siteId))
                                    .toJsonObject().put("_id", userGroup.getString("_id"));
                            } else {
                                throw badRequest("Create <Site> at first.");
                            }
                        })
                        .flatMap(userGroupObject -> dispatchRequests(HttpMethod.PUT, URL.put_user_group, userGroupObject))
                ).subscribe(ignore -> message.reply(new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())), throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void respondRequestWithSiteAndAssociateCompanyRepresentation(Message<Object> message, JsonObject query, String urn) {
        dispatchRequests(HttpMethod.POST, urn, query)
            .flatMap(response -> Observable.fromIterable(response.toJsonArray())
                .flatMapSingle(res -> {
                    JsonObject object = new JsonObject(res.toString());
                    return dispatchRequests(HttpMethod.GET, URL.get_site + "/" + object.getString("site_id"), null)
                        .flatMap(site -> {
                            if (StringUtils.isNotNull(site.toString())) {
                                object.put("site", buildSiteWithAbsoluteImageUri(message, site.toJsonObject()));
                            }
                            return dispatchRequests(HttpMethod.GET, URL.get_company + "/" + object.getString("associated_company_id"), null)
                                .map(associatedCompany -> object.put("associated_company", associatedCompany.toJsonObject()));
                        });
                }).toList()
            )
            .subscribe(response -> {
                    JsonArray array = new JsonArray();
                    response.forEach(array::add);
                    message.reply(new CustomMessage<>(null, array, HttpResponseStatus.OK.code()));
                },
                throwable -> handleHttpException(message, throwable));
    }

    private void respondRequestWithAssociateCompanyRepresentation(Message<Object> message, JsonObject query, String urn) {
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
                response.forEach(jsonObject -> array.add(buildSiteWithAbsoluteImageUri(message, jsonObject)));
                message.reply(new CustomMessage<>(null, array, HttpResponseStatus.OK.code()));
            },
            throwable -> handleHttpException(message, throwable));
    }

    private void respondRequestWithCompanyAssociateCompanyAndGroupRepresentation(Message<Object> message, JsonObject query, String urn) {
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
                                                            object.put("group", group.toJsonObject().put("site", buildSiteWithAbsoluteImageUri(message, site.toJsonObject())));
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
                message.reply(new CustomMessage<>(null, array, HttpResponseStatus.OK.code()));
            },
            throwable -> handleHttpException(message, throwable));
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
}
