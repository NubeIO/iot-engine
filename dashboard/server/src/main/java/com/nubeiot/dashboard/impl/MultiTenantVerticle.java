package com.nubeiot.dashboard.impl;

import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleForbiddenResponse;
import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleHttpException;
import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleNotFoundResponse;
import static com.nubeiot.dashboard.constants.Address.MULTI_TENANT_ADDRESS;
import static com.nubeiot.dashboard.constants.Collection.COMPANY;
import static com.nubeiot.dashboard.constants.Collection.SITE;
import static com.nubeiot.dashboard.constants.Collection.USER;
import static com.nubeiot.dashboard.constants.Collection.USER_GROUP;
import static com.nubeiot.dashboard.utils.MongoUtils.idQuery;

import java.util.List;
import java.util.stream.Collectors;

import com.nubeiot.core.common.HttpHelper;
import com.nubeiot.core.common.RxRestAPIVerticle;
import com.nubeiot.core.common.constants.Services;
import com.nubeiot.core.common.utils.CustomMessage;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.mongo.MongoUtils;
import com.nubeiot.core.utils.SQLUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.helpers.CustomMessageHelper;
import com.nubeiot.dashboard.models.Company;
import com.nubeiot.dashboard.models.KeycloakUserRepresentation;
import com.nubeiot.dashboard.models.MongoUser;
import com.nubeiot.dashboard.models.Site;
import com.nubeiot.dashboard.models.UserGroup;
import com.nubeiot.dashboard.utils.DittoUtils;
import com.nubeiot.dashboard.utils.UserUtils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.ext.mongo.MongoClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MultiTenantVerticle extends ContainerVerticle implements RxRestAPIVerticle {

    private static final String DEFAULT_PASSWORD = "helloworld";
    private final MicroContext microContext;
    private MongoClient mongoClient;
    private JsonObject appConfig;

    @Override
    public void start() {
        super.start();
        this.appConfig = this.nubeConfig.getAppConfig().toJson();
        mongoClient = MongoClient.createNonShared(vertx, this.appConfig.getJsonObject("mongo").getJsonObject("config"));
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
                    case "company":
                        handleUpdateCompany(message);
                        break;
                    case "change_default_site":
                        handleChangeDefaultSite(message);
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
        JsonObject headers = CustomMessageHelper.getHeaders(message);
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
                     .flatMap(ignored -> UserUtils.getUserFromUsername(body.getString("username"), accessToken,
                                                                       authServerUrl, realmName, client))
                     // 3. Resetting password; by default password: '<DEFAULT_PASSWORD>'
                     .flatMap(keycloakUser -> UserUtils.resetPassword(keycloakUser.getString("id"),
                                                                      body.getString("password", DEFAULT_PASSWORD),
                                                                      accessToken, authServerUrl, realmName, client)
                                                       .flatMap(ignored -> {
                                                           if (role == Role.SUPER_ADMIN) {
                                                               // 4.1 any user can be created
                                                               return createMongoUser(headers, user, body, accessToken,
                                                                                      authServerUrl, realmName, client,
                                                                                      keycloakUser,
                                                                                      new JsonObject().put("role",
                                                                                                           new JsonObject()
                                                                                                               .put(
                                                                                                                   "$not",
                                                                                                                   new JsonObject()
                                                                                                                       .put(
                                                                                                                           "$eq",
                                                                                                                           Role.SUPER_ADMIN
                                                                                                                               .toString()))));
                                                           } else if (role == Role.ADMIN) {
                                                               // 4.2 only child companies can make associate with
                                                               // it's users
                                                               return byAdminCompanyGetAdminWithManagerSelectionListQuery(
                                                                   companyId).flatMap(
                                                                   query -> createMongoUser(headers, user, body,
                                                                                            accessToken, authServerUrl,
                                                                                            realmName, client,
                                                                                            keycloakUser, query));
                                                           } else {
                                                               // 4.3 Creating user on MongoDB with 'group_id'
                                                               return createMongoUserByManager(headers, body, user,
                                                                                               accessToken, client,
                                                                                               authServerUrl, realmName,
                                                                                               keycloakUser,
                                                                                               user.getString(
                                                                                                   "company_id"));
                                                           }
                                                       }))
                     .subscribe(statusCode -> message.reply(new CustomMessage<>(null, new JsonObject(), statusCode)),
                                throwable -> handleHttpException(message, throwable));
        }
    }

    private SingleSource<? extends Integer> createMongoUser(JsonObject headers, JsonObject user, JsonObject body,
                                                            String accessToken, String authServerUrl, String realmName,
                                                            HttpClient client, JsonObject keycloakUser,
                                                            JsonObject query) {
        return mongoClient.rxFind(COMPANY, query).flatMap(childCompanies -> {
            if (childCompanies.size() > 0) {
                // 5.1 Proceed for creating MongoDB user
                String[] childCompaniesIds = MongoUtils.getIds(childCompanies);
                String companyId = SQLUtils.getMatchValueOrFirstOne(body.getString("company_id", ""),
                                                                    childCompaniesIds);
                JsonObject companyJsonObject = MongoUtils.getMatchValueOrFirstOne(childCompanies, companyId);

                body.put("company_id", companyJsonObject.getString("_id"));
                if (companyJsonObject.getString("role").equals(Role.MANAGER.toString()) &&
                    SQLUtils.in(body.getString("role", ""), Role.MANAGER.toString(), Role.USER.toString(),
                                Role.GUEST.toString(), "")) {

                    JsonArray sitesIds = body.getJsonArray("sites_ids", new JsonArray());
                    if (sitesIds.size() == 0 && Strings.isNotBlank(body.getString("site_id"))) {
                        sitesIds = new JsonArray().add(body.getString("site_id"));
                    } else if (sitesIds.size() == 0) {
                        return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName,
                                                    client).map(ign -> {
                            throw HttpHelper.badRequest("You must include valid sites_ids on the request data.");
                        });
                    }
                    JsonArray sitesIds$ = sitesIds;

                    return mongoClient.rxFind(SITE, new JsonObject().put("_id", new JsonObject().put("$in", sitesIds)))
                                      .flatMap(childSites -> {
                                          String siteId = body.getString("site_id");
                                          if (childSites != null) {
                                              boolean isSiteAssociated = true;
                                              for (JsonObject childSite : childSites) {
                                                  if (!childSite.getString("associated_company_id")
                                                                .equals(companyJsonObject.getString("_id"))) {
                                                      isSiteAssociated = false;
                                                      break;
                                                  }
                                              }
                                              if (isSiteAssociated && sitesIds$.contains(siteId)) {
                                                  return Single.just(body.put("role", body.getString("role",
                                                                                                     Role.MANAGER.toString()))); // if nothing then it should be MANAGER
                                              } else {
                                                  return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken,
                                                                              authServerUrl, realmName, client)
                                                                  .map(ign -> {
                                                                      throw HttpHelper.forbidden();
                                                                  });
                                              }
                                          } else {
                                              return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken,
                                                                          authServerUrl, realmName, client).map(ign -> {
                                                  throw HttpHelper.badRequest("Site doesn't exist.");
                                              });
                                          }
                                      })
                                      .flatMap(siteEditedBody -> {
                                          if (SQLUtils.in(body.getString("role", ""), Role.USER.toString(),
                                                          Role.GUEST.toString())) {
                                              String groupId = body.getString("group_id");
                                              if (groupId == null) {
                                                  return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken,
                                                                              authServerUrl, realmName, client)
                                                                  .map(ign -> {
                                                                      throw HttpHelper.badRequest(
                                                                          "You must include group_id on the request " +
                                                                          "data.");
                                                                  });
                                              }

                                              return mongoClient.rxFind(USER_GROUP, new JsonObject().put("site_id",
                                                                                                         body.getString(
                                                                                                             "site_id"
                                                                                                                       )))
                                                                .flatMap(childUserGroups -> {
                                                                    if (childUserGroups.size() > 0) {
                                                                        if (MongoUtils.getIdsOnList(childUserGroups)
                                                                                      .contains(groupId)) {
                                                                            return Single.just(
                                                                                siteEditedBody.put("group_id", groupId)
                                                                                              .put(
                                                                                                  "associated_company_id",
                                                                                                  companyJsonObject.getString(
                                                                                                      "_id")));
                                                                            // For USER and
                                                                            // GUEST
                                                                            // company_id
                                                                            // and
                                                                            // associated_company_id be same
                                                                        } else {
                                                                            return UserUtils.deleteUser(
                                                                                keycloakUser.getString("id"),
                                                                                accessToken, authServerUrl, realmName,
                                                                                client).map(ign -> {
                                                                                throw HttpHelper.badRequest(
                                                                                    "<UserGroup> doesn't exist on " +
                                                                                    "that <Site>.");
                                                                            });
                                                                        }
                                                                    } else {
                                                                        return UserUtils.deleteUser(
                                                                            keycloakUser.getString("id"), accessToken,
                                                                            authServerUrl, realmName,
                                                                            client) // For ADMIN company
                                                                                        .map(ign -> {
                                                                                            throw HttpHelper.badRequest(
                                                                                                "<Site> doesn't have " +
                                                                                                "any <UserGroup>.");
                                                                                        });
                                                                    }
                                                                });
                                          } else {
                                              return Single.just(siteEditedBody.put("group_id", "")
                                                                               .put("associated_company_id",
                                                                                    companyJsonObject.getString(
                                                                                        "associated_company_id")));
                                          }
                                      });
                } else if (companyJsonObject.getString("role").equals(Role.ADMIN.toString())) {
                    return Single.just(
                        body.put("associated_company_id", companyJsonObject.getString("associated_company_id"))
                            .put("role", companyJsonObject.getString("role"))
                            .put("site_id", "")
                            .put("group_id", ""));
                } else {
                    return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName,
                                                client).map(ign -> {
                        throw HttpHelper.badRequest("Condition doesn't match up.");
                    });
                }
            } else {
                // 5.2 Remove user from Keycloak
                return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName, client)
                                .map(ign -> {
                                    throw new HttpException(HttpResponseStatus.BAD_REQUEST,
                                                            "Create <Company> at first.");
                                });
            }
        }).flatMap(editedBody -> {
            JsonObject mongoUser = new MongoUser(editedBody, user, keycloakUser).toJsonObject();
            return mongoClient.rxSave(USER, mongoUser).flatMap(ignored -> {
                if (appConfig.getBoolean("ditto-policy")) {
                    if (Role.ADMIN == Role.valueOf(mongoUser.getString("role"))) {
                        return byAdminCompanyGetAdminWithManagerSelectionListQuery(
                            mongoUser.getString("company_id")).flatMap(subQuery -> mongoClient.rxFind(SITE, subQuery))
                                                              .flatMap(sites -> Observable.fromIterable(sites)
                                                                                          .flatMapSingle(
                                                                                              site -> dispatchRequests(
                                                                                                  microContext,
                                                                                                  HttpMethod.PUT,
                                                                                                  headers,
                                                                                                  Services.POLICY_PREFIX +
                                                                                                  site.getString(
                                                                                                      "_id") +
                                                                                                  "/entries/admin" +
                                                                                                  "/subjects/nginx:" +
                                                                                                  mongoUser.getString(
                                                                                                      "username"),
                                                                                                  new JsonObject().put(
                                                                                                      "type", "admin")))
                                                                                          .toList());
                    } else {
                        return putSubjectOnPolicy(headers, mongoUser);
                    }
                } else {
                    return Single.just("");
                }
            }).map(ignore -> HttpResponseStatus.CREATED.code());
        });
    }

    private SingleSource<? extends Integer> createMongoUserByManager(JsonObject headers, JsonObject body,
                                                                     JsonObject user, String accessToken,
                                                                     HttpClient client, String authServerUrl,
                                                                     String realmName, JsonObject keycloakUser,
                                                                     String companyId) {
        JsonObject query = new JsonObject().put("associated_company_id", companyId);
        return mongoClient.rxFind(USER_GROUP, query).flatMap(childGroups -> {
            if (childGroups.size() > 0) {
                // 5.1 Creating user on MongoDB
                body.put("company_id", companyId)
                    .put("associated_company_id", companyId)
                    .put("site_id", CustomMessageHelper.getSiteId(user))
                    .put("group_id", SQLUtils.getMatchValueOrFirstOne(body.getString("group_id", ""),
                                                                      MongoUtils.getIds(childGroups)));
                JsonObject mongoUser = new MongoUser(body, user, keycloakUser).toJsonObject();
                return mongoClient.rxSave(USER, mongoUser).flatMap(ignored -> {
                    if (appConfig.getBoolean("ditto-policy")) {
                        return putSubjectOnPolicy(headers, mongoUser);
                    } else {
                        return Single.just("");
                    }
                }).map(ignore -> HttpResponseStatus.CREATED.code());
            } else {
                // 5.2 Remove user from Keycloak
                return UserUtils.deleteUser(keycloakUser.getString("id"), accessToken, authServerUrl, realmName, client)
                                .map(ign -> {
                                    throw new HttpException(HttpResponseStatus.BAD_REQUEST,
                                                            "Create <User Group> at first.");
                                });
            }
        });
    }

    private SingleSource<?> putSubjectOnPolicy(JsonObject headers, JsonObject mongoUser) {
        String entry = mongoUser.getString("role").toLowerCase();
        if (entry.equals(Role.GUEST.toString().toLowerCase())) {
            entry = "user";
        }
        return dispatchRequests(microContext, HttpMethod.PUT, headers,
                                Services.POLICY_PREFIX + mongoUser.getString("site_id") + "/entries/" + entry +
                                "/subjects/nginx:" + mongoUser.getString("username"),
                                new JsonObject().put("type", mongoUser.getString("role").toLowerCase()));
    }

    private void handlePostCompany(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);

        if (role == Role.SUPER_ADMIN) {
            JsonObject body = CustomMessageHelper.getBodyAsJson(message);
            String associatedCompanyId = body.getString("associated_company_id", "");
            if (Strings.isNotBlank(associatedCompanyId)) {
                mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                           .flatMap(companyResponse -> {
                               if (companyResponse == null) {
                                   throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(),
                                                           "Failed to get the associated_company");
                               } else {
                                   Company company = new Company(body.put("role", UserUtils.getRole(
                                       Role.valueOf(companyResponse.getString("role")))));
                                   return mongoClient.rxSave(COMPANY, company.toJsonObject());
                               }
                           })
                           .subscribe(ignore -> message.reply(
                               new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.OK.code())),
                                      throwable -> handleHttpException(message, throwable));
            } else {
                Company company = new Company(CustomMessageHelper.getBodyAsJson(message)
                                                                 .put("associated_company_id",
                                                                      CustomMessageHelper.getCompanyId(user))
                                                                 .put("role", Role.ADMIN.toString()));
                mongoClient.rxSave(COMPANY, company.toJsonObject())
                           .subscribe(ignore -> message.reply(
                               new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.OK.code())),
                                      throwable -> handleHttpException(message, throwable));
            }
        } else if (role == Role.ADMIN) {
            Company company = new Company(CustomMessageHelper.getBodyAsJson(message)
                                                             .put("associated_company_id",
                                                                  CustomMessageHelper.getCompanyId(user))
                                                             .put("role", Role.MANAGER.toString()));

            mongoClient.rxSave(COMPANY, company.toJsonObject())
                       .subscribe(ignore -> message.reply(
                           new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.OK.code())),
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
        JsonObject headers = CustomMessageHelper.getHeaders(message);

        if (role == Role.SUPER_ADMIN || role == Role.ADMIN) {
            String associatedCompanyId = body.getString("associated_company_id");
            mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                       .map(associatedCompany -> {
                           if (associatedCompany != null) {
                               if (associatedCompany.getString("role").equals(Role.MANAGER.toString())) {
                                   return associatedCompany;
                               } else {
                                   throw HttpHelper.badRequest("You must associate Manager level company.");
                               }
                           } else {
                               throw HttpHelper.badRequest("Failed to get the associated_company!");
                           }
                       })
                       .flatMap(associatedCompany -> {
                           if (role == Role.SUPER_ADMIN) {
                               return Single.just(associatedCompany);
                           } else {
                               return byAdminCompanyGetManagerSelectionList(companyId).map(companies -> {
                                   if (companies.contains(associatedCompany.getString("_id"))) {
                                       return associatedCompany;
                                   } else {
                                       throw HttpHelper.forbidden();
                                   }
                               });
                           }
                       })
                       .flatMap(associatedCompany -> mongoClient.rxSave(SITE, new Site(
                           body.put("associated_company_id", associatedCompany.getString("_id"))
                               .put("role", Role.MANAGER.toString())).toJsonObject()).flatMap(siteId -> {
                           if (appConfig.getBoolean("ditto-policy")) {
                               // We will create a fresh ditto policy for Site
                               return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompany.getString("_id")), null)
                                                 .flatMap(managerLevelCompany -> mongoClient.rxFindOne(COMPANY, idQuery(
                                                     managerLevelCompany.getString("associated_company_id")), null)
                                                                                            .flatMap(
                                                                                                adminLevelCompany -> mongoClient
                                                                                                                         .rxFind(
                                                                                                                             USER,
                                                                                                                             new JsonObject()
                                                                                                                                 .put(
                                                                                                                                     "$or",
                                                                                                                                     new JsonArray()
                                                                                                                                         .add(
                                                                                                                                             new JsonObject()
                                                                                                                                                 .put(
                                                                                                                                                     "role",
                                                                                                                                                     Role.SUPER_ADMIN
                                                                                                                                                         .toString()))
                                                                                                                                         .add(
                                                                                                                                             new JsonObject()
                                                                                                                                                 .put(
                                                                                                                                                     "company_id",
                                                                                                                                                     adminLevelCompany
                                                                                                                                                         .getString(
                                                                                                                                                             "_id")))))
                                                                                                                         .flatMap(
                                                                                                                             users -> dispatchRequests(
                                                                                                                                 microContext,
                                                                                                                                 HttpMethod.PUT,
                                                                                                                                 headers,
                                                                                                                                 Services.POLICY_PREFIX +
                                                                                                                                 siteId,
                                                                                                                                 DittoUtils
                                                                                                                                     .createPolicy(
                                                                                                                                         users)))
                                                                                                                         .doOnError(
                                                                                                                             throwable -> mongoClient
                                                                                                                                              .remove(
                                                                                                                                                  SITE,
                                                                                                                                                  idQuery(
                                                                                                                                                      siteId),
                                                                                                                                                  null))));
                           }
                           return Single.just(siteId);
                       }))
                       .subscribe(ignore -> message.reply(
                           new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.CREATED.code())),
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
            Single.create(source -> {
                if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                    getManagerSiteQuery(role, userCompanyId).flatMap(
                        managerSiteQuery -> mongoClient.rxFind(SITE, managerSiteQuery).flatMap(childSitesResponse -> {
                            if (childSitesResponse.size() > 0) {
                                String[] availableSites = MongoUtils.getIds(childSitesResponse);
                                String siteId = SQLUtils.getMatchValue(
                                    CustomMessageHelper.getBodyAsJson(message).getString("site_id", ""),
                                    availableSites);
                                if (siteId == null) {
                                    throw HttpHelper.badRequest("Site doesn't match up Exception!");
                                }
                                String associatedCompanyId = body.getString("associated_company_id", "");
                                if (Strings.isNotBlank(associatedCompanyId)) {
                                    return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                                                      .map(response -> {
                                                          if (response != null) {
                                                              if (response.getString("role")
                                                                          .equals(Role.MANAGER.toString()) &&
                                                                  (role == Role.SUPER_ADMIN || (role == Role.ADMIN &&
                                                                                                managerSiteQuery.getJsonObject(
                                                                                                    "associated_company_id")
                                                                                                                .getJsonArray(
                                                                                                                    "$in")
                                                                                                                .contains(
                                                                                                                    associatedCompanyId)))) {
                                                                  return new UserGroup(body.put("associated_company_id",
                                                                                                associatedCompanyId)
                                                                                           .put("site_id", siteId));
                                                              } else {
                                                                  throw HttpHelper.badRequest(
                                                                      "We should assign Manager level company");
                                                              }
                                                          } else {
                                                              throw HttpHelper.badRequest(
                                                                  "We don't have the associated_company_id");
                                                          }
                                                      });
                                } else {
                                    throw HttpHelper.badRequest("No associated_company_id value is requested.");
                                }
                            } else {
                                throw HttpHelper.badRequest("Create <Site> at first.");
                            }
                        })).subscribe(source::onSuccess, source::onError);
                } else {
                    source.onSuccess(new UserGroup(body.put("associated_company_id", userCompanyId)
                                                       .put("site_id", CustomMessageHelper.getSiteId(user))));
                }
            })
                  .flatMap(userGroup -> mongoClient.rxSave(USER_GROUP, ((UserGroup) userGroup).toJsonObject()))
                  .subscribe(ignore -> message.reply(
                      new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.CREATED.code())),
                             throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleGetCompanies(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        Single.just(new JsonObject()).flatMap(ignore -> {
            if (role == Role.SUPER_ADMIN) {
                return mongoClient.rxFind(COMPANY, new JsonObject().put("role", new JsonObject().put("$not",
                                                                                                     new JsonObject().put(
                                                                                                         "$eq",
                                                                                                         Role.SUPER_ADMIN
                                                                                                             .toString()))));
            } else if (role == Role.ADMIN) {
                return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId));
            } else {
                throw HttpHelper.forbidden();
            }
        }).flatMap(response -> Observable.fromIterable(response).flatMapSingle(company -> {
            String associatedCompanyId = company.getString("associated_company_id");
            return associatedCompanyRepresentation(company, associatedCompanyId);
        }).toList()).subscribe(response -> {
            message.reply(new CustomMessage<>(null, response, HttpResponseStatus.OK.code()));
        }, throwable -> handleHttpException(message, throwable));
    }

    private void handleGetUsers(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        if (role == Role.SUPER_ADMIN) {
            respondRequestWithCompanyAssociateCompanyGroupAndSiteRepresentation(message, new JsonObject().put("role",
                                                                                                              new JsonObject()
                                                                                                                  .put(
                                                                                                                      "$not",
                                                                                                                      new JsonObject()
                                                                                                                          .put(
                                                                                                                              "$eq",
                                                                                                                              Role.SUPER_ADMIN
                                                                                                                                  .toString()))),
                                                                                USER);
        } else if (role == Role.ADMIN) {
            // Returning all <Users> which is branches from the ADMIN
            mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId)
                                                        .put("role", Role.MANAGER.toString()))
                       .subscribe(
                           companies -> respondRequestWithCompanyAssociateCompanyGroupAndSiteRepresentation(message,
                                                                                                            new JsonObject()
                                                                                                                .put(
                                                                                                                    "associated_company_id",
                                                                                                                    new JsonObject()
                                                                                                                        .put(
                                                                                                                            "$in",
                                                                                                                            MongoUtils
                                                                                                                                .getIdsOnJsonArray(
                                                                                                                                    companies)
                                                                                                                                .add(
                                                                                                                                    companyId))),
                                                                                                            USER),
                           throwable -> handleHttpException(message, throwable));
        } else if (role == Role.MANAGER) {
            respondRequestWithCompanyAssociateCompanyGroupAndSiteRepresentation(message, new JsonObject().put(
                "associated_company_id", companyId), USER);
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleGetSites(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        Single.just(new JsonObject()).flatMap(ignored -> {
            if (role == Role.SUPER_ADMIN) {
                return mongoClient.rxFind(SITE, new JsonObject().put("role", Role.MANAGER.toString()));
            } else if (role == Role.ADMIN) {
                return byAdminCompanyGetManagerSelectionListQuery(companyId).flatMap(
                    query -> mongoClient.rxFind(SITE, query));
            } else if (role == Role.MANAGER) {
                return mongoClient.rxFind(SITE, new JsonObject().put("associated_company_id", companyId));
            } else {
                throw HttpHelper.forbidden();
            }
        }).flatMap(sites -> Observable.fromIterable(sites).flatMapSingle(site -> {
            String associatedCompanyId = site.getString("associated_company_id");
            return associatedCompanyRepresentation(site, associatedCompanyId);
        }).toList()).subscribe(sites -> {
            message.reply(new CustomMessage<>(null, sites, HttpResponseStatus.OK.code()));
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
                      return byAdminCompanyGetManagerSelectionListQuery(companyId).flatMap(
                          query -> mongoClient.rxFind(USER_GROUP, query));
                  } else if (role == Role.MANAGER) {
                      return mongoClient.rxFind(USER_GROUP, new JsonObject().put("associated_company_id", companyId));
                  } else {
                      return mongoClient.rxFind(USER_GROUP, new JsonObject().put("_id", user.getString("group_id")));
                  }
              })
              .flatMap(userGroups -> Observable.fromIterable(userGroups)
                                               .flatMapSingle(userGroup -> mongoClient.rxFindOne(SITE, idQuery(
                                                   userGroup.getString("site_id")), null).flatMap(site -> {

                                                   String associatedCompanyId = userGroup.getString(
                                                       "associated_company_id");
                                                   return associatedCompanyRepresentation(userGroup,
                                                                                          associatedCompanyId).map(
                                                       ignored -> {
                                                           if (site != null) {
                                                               return userGroup.put("site", site);
                                                           }
                                                           return userGroup;
                                                       });
                                               }))
                                               .toList())
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
                           throw new HttpException(HttpResponseStatus.BAD_REQUEST,
                                                   "Doesn't have those Users on Database.");
                       })
                       .flatMap(users -> Observable.fromIterable(users)
                                                   .flatMapSingle(userObject -> deleteUserFromKeycloakAndMongo(message,
                                                                                                               userObject))
                                                   .toList())
                       .subscribe(ignored -> message.reply(
                           new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                                  throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private SingleSource<? extends Integer> deleteUserFromKeycloakAndMongo(Message<Object> message, Object userObject) {
        JsonObject userObjectJson = (JsonObject) (userObject);
        JsonObject user = CustomMessageHelper.getUser(message);
        String accessToken = CustomMessageHelper.getAccessToken(user);
        JsonObject keycloakConfig = CustomMessageHelper.getKeycloakConfig(message);
        JsonObject headers = CustomMessageHelper.getHeaders(message);

        HttpClient client = vertx.createHttpClient();

        return UserUtils.deleteUser(userObjectJson.getString("_id"), accessToken,
                                    keycloakConfig.getString("auth-server-url"), keycloakConfig.getString("realm"),
                                    client).flatMap(deleteUserKeycloakResponse -> {
            if (deleteUserKeycloakResponse.getInteger("statusCode") == HttpResponseStatus.NO_CONTENT.code()) {
                JsonObject queryToDeleteOne = new JsonObject().put("_id", new JsonObject().put("$in",
                                                                                               new JsonArray().add(
                                                                                                   userObjectJson.getString(
                                                                                                       "_id"))));

                return mongoClient.rxRemoveDocuments(USER, queryToDeleteOne).flatMap(ignored -> {
                    if (appConfig.getBoolean("ditto-policy")) {
                        if (Role.ADMIN == Role.valueOf(userObjectJson.getString("role"))) {
                            return byAdminCompanyGetAdminWithManagerSelectionListQuery(
                                userObjectJson.getString("company_id")).flatMap(
                                subQuery -> mongoClient.rxFind(SITE, subQuery))
                                                                       .flatMap(sites -> Observable.fromIterable(sites)
                                                                                                   .flatMapSingle(
                                                                                                       site -> dispatchRequests(
                                                                                                           microContext,
                                                                                                           HttpMethod.DELETE,
                                                                                                           headers,
                                                                                                           Services.POLICY_PREFIX +
                                                                                                           site.getString(
                                                                                                               "_id") +
                                                                                                           "/entries" +
                                                                                                           "/admin" +
                                                                                                           "/subjects" +
                                                                                                           "/nginx:" +
                                                                                                           userObjectJson
                                                                                                               .getString(
                                                                                                                   "username"),
                                                                                                           null))
                                                                                                   .toList());
                        } else {
                            String entry = userObjectJson.getString("role").toLowerCase();
                            if (entry.equals(Role.GUEST.toString().toLowerCase())) {
                                entry = "user";
                            }
                            return dispatchRequests(microContext, HttpMethod.DELETE, headers,
                                                    Services.POLICY_PREFIX + userObjectJson.getString("site_id") +
                                                    "/entries/" + entry + "/subjects/nginx:" +
                                                    userObjectJson.getString("username"), null);
                        }
                    } else {
                        return Single.just("");
                    }
                }).map(deleteUserResponse -> HttpResponseStatus.NO_CONTENT.code());
            } else {
                throw new HttpException(deleteUserKeycloakResponse.getInteger("statusCode"),
                                        "Users are unable to deleted from the services.");
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
                               throw HttpHelper.badRequest("Doesn't have those <Companies> on Database.");
                           }
                       })
                       .flatMap(companies -> mongoClient.rxRemoveDocuments(COMPANY, query))
                       .subscribe(ignore -> message.reply(
                           new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                                  throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private SingleSource<? extends List<JsonObject>> checkPermissionAndReturnValue(Role role, String companyId,
                                                                                   List<JsonObject> objects) {
        return Observable.fromIterable(objects)
                         .flatMapSingle(
                             jsonObject -> objectLevelPermission(role, jsonObject.getString("associated_company_id"),
                                                                 companyId).map(permitted -> {
                                 if (!permitted) {
                                     throw HttpHelper.forbidden();
                                 }
                                 return jsonObject;
                             }))
                         .toList();
    }

    private void handleDeleteSites(Message<Object> message) {
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        JsonObject headers = CustomMessageHelper.getHeaders(message);

        // Model level permission
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            JsonArray queryInput = CustomMessageHelper.getBodyAsJsonArray(message);
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            mongoClient.rxFind(SITE, query)
                       .flatMap(sites -> {
                           if (sites.size() == queryInput.size()) {
                               return checkPermissionAndReturnValue(role, companyId, sites);
                           } else {
                               throw HttpHelper.badRequest("Doesn't have those <Sites> on Database.");
                           }
                       })
                       .flatMap(ignored -> Observable.fromIterable(queryInput)
                                                     .flatMapSingle(id -> mongoClient.rxRemoveDocument(SITE, idQuery(
                                                         id.toString())).flatMap(ign -> {
                                                         if (appConfig.getBoolean("ditto-policy")) {
                                                             return dispatchRequests(microContext, HttpMethod.DELETE,
                                                                                     headers,
                                                                                     Services.POLICY_PREFIX + id, null);
                                                         } else {
                                                             return Single.just("");
                                                         }
                                                     }))
                                                     .toList())
                       .subscribe(ignored -> message.reply(
                           new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
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
                               throw HttpHelper.badRequest("Doesn't have those <User Groups> on Database.");
                           }
                       })
                       .flatMap(ignored -> mongoClient.rxRemoveDocuments(USER_GROUP, query))
                       .subscribe(ignored -> message.reply(
                           new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                                  throwable -> handleHttpException(message, throwable));
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

        UserUtils.queryUsers(query, accessToken, authServerUrl, realmName, client).subscribe(users -> {
            logger.info("Users: " + users);
            int usersSize = users.stream().filter(userObject -> {
                JsonObject jsonUser = (JsonObject) userObject;
                if (Strings.isBlank(username)) {
                    return jsonUser.getString("email").equals(email);
                } else if (Strings.isBlank(email)) {
                    return jsonUser.getString("username").equals(username);
                } else {
                    return jsonUser.getString("username").equals(username) && jsonUser.getString("email").equals(email);
                }
            }).collect(Collectors.toList()).size();
            logger.info("Size of user match: " + usersSize);
            if (usersSize > 0) {
                message.reply(
                    new CustomMessage<>(null, new JsonObject().put("exist", true), HttpResponseStatus.OK.code()));
            } else {
                message.reply(
                    new CustomMessage<>(null, new JsonObject().put("exist", false), HttpResponseStatus.OK.code()));
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

        if (Strings.isNotBlank(password)) {
            mongoClient.rxFindOne(USER, idQuery(userId), null)
                       .map(response -> {
                           if (response == null) {
                               throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Not found");
                           } else {
                               return response;
                           }
                       })
                       .flatMap(user -> {
                           // Own password can be changed or those users passwords which is associated with some company
                           if (ctxUser.getString("user_id").equals(userId)) {
                               return UserUtils.resetPassword(userId, password, accessToken, authServerUrl, realmName,
                                                              client);
                           } else {
                               return objectLevelPermission(role, user.getString("associated_company_id"),
                                                            ctxUser.getString("company_id")).map(permitted -> {
                                   if (permitted) {
                                       return UserUtils.resetPassword(userId, password, accessToken, authServerUrl,
                                                                      realmName, client);
                                   } else {
                                       throw HttpHelper.forbidden();
                                   }
                               });
                           }
                       })
                       .subscribe(ignored -> message.reply(
                           new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                                  throwable -> handleHttpException(message, throwable));
        } else {
            message.reply(new CustomMessage<>(null, new JsonObject().put("message", "Password can't be NULL."),
                                              HttpResponseStatus.BAD_REQUEST.code()));
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
                       if (response == null) {
                           throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Invalid user_id.");
                       } else {
                           return response;
                       }
                   })
                   .flatMap(user -> {
                       logger.info("Responded user: " + user);
                       // Own user_profile can be changed or those users_profiles which is associated with same company
                       if (ctxUser.getString("user_id").equals(userId) || (role == Role.MANAGER) &&
                                                                          ctxUser.getString("company_id")
                                                                                 .equals(
                                                                                     CustomMessageHelper.getAssociatedCompanyId(
                                                                                         user)) ||
                           role.toString().equals(Role.SUPER_ADMIN.toString())) {

                           return UserUtils.updateUser(userId, keycloakUserRepresentation, accessToken, authServerUrl,
                                                       realmName, client);
                       } else if (role == Role.ADMIN) {
                           return byAdminCompanyGetAdminWithManagerSelectionList(CustomMessageHelper.getCompanyId(user))
                                      .flatMap(response -> {
                                          if (response.contains(CustomMessageHelper.getCompanyId(user))) {
                                              return UserUtils.updateUser(userId, keycloakUserRepresentation,
                                                                          accessToken, authServerUrl, realmName,
                                                                          client);
                                          } else {
                                              throw HttpHelper.forbidden();
                                          }
                                      });
                       } else {
                           throw HttpHelper.forbidden();
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
                               return updateMongoUser(ctxUser, role, body, keycloakUser, new JsonObject().put("role",
                                                                                                              new JsonObject()
                                                                                                                  .put(
                                                                                                                      "$not",
                                                                                                                      new JsonObject()
                                                                                                                          .put(
                                                                                                                              "$eq",
                                                                                                                              Role.SUPER_ADMIN
                                                                                                                                  .toString()))));
                           } else if (role == Role.ADMIN) {
                               return byAdminCompanyGetAdminWithManagerSelectionListQuery(
                                   CustomMessageHelper.getCompanyId(ctxUser)).flatMap(
                                   query -> updateMongoUser(ctxUser, role, body, keycloakUser, query));
                           } else {
                               // Only child <User Groups> can be added by the parent
                               return updateMongoUserByManager(ctxUser, body, keycloakUser,
                                                               ctxUser.getString("company_id"));
                           }
                       } else {
                           return updateOwnUser(body, ctxUser, keycloakUser);
                       }
                   })
                   .subscribe(statusCode -> message.reply(new CustomMessage<>(null, new JsonObject(), statusCode)),
                              throwable -> handleHttpException(message, throwable));
    }

    private SingleSource<? extends Integer> updateMongoUser(JsonObject ctxUser, Role role, JsonObject body,
                                                            JsonObject keycloakUser, JsonObject query) {
        return mongoClient.rxFind(COMPANY, query).flatMap(childCompanies -> {
            if (childCompanies.size() > 0) {
                String[] _ids = MongoUtils.getIds(childCompanies);
                String companyId = SQLUtils.getMatchValueOrFirstOne(body.getString("company_id", ""), _ids);
                JsonObject companyJsonObject = MongoUtils.getMatchValueOrFirstOne(childCompanies, companyId);

                body.put("company_id", companyJsonObject.getString("_id"));

                if (companyJsonObject.getString("role").equals(Role.MANAGER.toString()) &&
                    SQLUtils.in(body.getString("role", ""), Role.MANAGER.toString(), Role.USER.toString(),
                                Role.GUEST.toString(), "")) {
                    JsonArray sitesIds = body.getJsonArray("sites_ids", new JsonArray());
                    if (sitesIds.size() == 0 && Strings.isNotBlank(body.getString("site_id"))) {
                        sitesIds = new JsonArray().add(body.getString("site_id"));
                    } else if (sitesIds.size() == 0) {
                        throw HttpHelper.badRequest("You must include site_id on the request data.");
                    }
                    JsonArray sitesIds$ = sitesIds;

                    return mongoClient.rxFind(SITE, new JsonObject().put("_id", new JsonObject().put("$in", sitesIds)))
                                      .flatMap(childSites -> {
                                          if (childSites != null) {
                                              String siteId = body.getString("site_id");
                                              boolean isSiteAssociated = true;
                                              for (JsonObject childSite : childSites) {
                                                  if (!childSite.getString("associated_company_id")
                                                                .equals(companyJsonObject.getString("_id"))) {
                                                      isSiteAssociated = false;
                                                      break;
                                                  }
                                              }
                                              if (isSiteAssociated && sitesIds$.contains(siteId)) {
                                                  return Single.just(body.put("role", body.getString("role",
                                                                                                     Role.MANAGER.toString()))); // if nothing then it should be MANAGER
                                              } else {
                                                  throw HttpHelper.forbidden();
                                              }
                                          } else {
                                              throw HttpHelper.badRequest("Site doesn't exist.");
                                          }
                                      })
                                      .flatMap(siteEditedBody -> {
                                          if (SQLUtils.in(body.getString("role", ""), Role.USER.toString(),
                                                          Role.GUEST.toString())) {
                                              String groupId = body.getString("group_id");
                                              if (groupId == null) {
                                                  throw HttpHelper.badRequest(
                                                      "You must include group_id on the request data.");
                                              }

                                              return mongoClient.rxFind(USER_GROUP, new JsonObject().put("site_id",
                                                                                                         body.getString(
                                                                                                             "site_id"
                                                                                                                       )))
                                                                .flatMap(childUserGroups -> {
                                                                    if (childUserGroups.size() > 0) {
                                                                        if (MongoUtils.getIdsOnList(childUserGroups)
                                                                                      .contains(groupId)) {
                                                                            return Single.just(
                                                                                siteEditedBody.put("group_id", groupId)
                                                                                              .put(
                                                                                                  "associated_company_id",
                                                                                                  companyJsonObject.getString(
                                                                                                      "_id")));
                                                                            // For USER and
                                                                            // GUEST
                                                                            // company_id
                                                                            // and
                                                                            // associated_company_id be same
                                                                        } else {
                                                                            throw HttpHelper.badRequest(
                                                                                "<UserGroup> doesn't exist on that " +
                                                                                "<Site>.");
                                                                        }
                                                                    } else {
                                                                        throw HttpHelper.badRequest(
                                                                            "<Site> doesn't have any <UserGroup>.");
                                                                    }
                                                                });
                                          } else {
                                              return Single.just(siteEditedBody.put("group_id", "")
                                                                               .put("associated_company_id",
                                                                                    companyJsonObject.getString(
                                                                                        "associated_company_id")));
                                              // For ADMIN company
                                          }
                                      });
                } else if (companyJsonObject.getString("role").equals(Role.ADMIN.toString())) {
                    return Single.just(
                        body.put("associated_company_id", companyJsonObject.getString("associated_company_id"))
                            .put("role", companyJsonObject.getString("role"))
                            .put("site_id", "")
                            .put("group_id", ""));
                } else {
                    throw HttpHelper.badRequest("Condition doesn't match up.");
                }
            } else {
                // This case shouldn't be happened; otherwise only half operation will be successful
                throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <Company> at first.");
            }
        }).flatMap(response -> {
            MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
            return mongoClient.rxSave(USER, mongoUser.toJsonObject())
                              .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
        });
    }

    private SingleSource<? extends Integer> updateMongoUserByManager(JsonObject ctxUser, JsonObject body,
                                                                     JsonObject keycloakUser, String companyId) {
        JsonObject query = new JsonObject().put("associated_company_id", companyId);

        return mongoClient.rxFind(USER, query).flatMap(childGroups -> {
            if (childGroups.size() > 0) {
                body.put("company_id", companyId)
                    .put("associated_company_id", companyId)
                    .put("site_id", CustomMessageHelper.getSiteId(ctxUser))
                    .put("group_id", SQLUtils.getMatchValueOrFirstOne(body.getString("group_id", ""),
                                                                      MongoUtils.getIds(childGroups)));
                MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
                return mongoClient.rxSave(USER, mongoUser.toJsonObject())
                                  .map(buffer -> HttpResponseStatus.NO_CONTENT.code());
            } else {
                throw new HttpException(HttpResponseStatus.BAD_REQUEST, "Create <User Group> at first.");
            }
        });
    }

    private SingleSource<? extends Integer> updateOwnUser(JsonObject body, JsonObject ctxUser,
                                                          JsonObject keycloakUser) {
        // User doesn't have the authority to update own company_id, associated_company_id, and group_id
        body.put("company_id", ctxUser.getString("company_id"))
            .put("associated_company_id", ctxUser.getString("associated_company_id"))
            .put("site_id", ctxUser.getString("site_id", ""))
            .put("group_id", ctxUser.getString("group_id", ""))
            .put("role", ctxUser.getString("role"));
        MongoUser mongoUser = new MongoUser(body, ctxUser, keycloakUser);
        JsonObject mongoUserObject = mongoUser.toJsonObject()
                                              .put("role", ctxUser.getString("role")); // Role shouldn't be overridden
        return mongoClient.rxSave(USER, mongoUserObject).map(buffer -> HttpResponseStatus.NO_CONTENT.code());
    }

    private void handleUpdateSite(Message<Object> message) {
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            String siteId = CustomMessageHelper.getParamsId(message);
            mongoClient.rxFindOne(SITE, idQuery(siteId), null)
                       .flatMap(site -> {
                           if (site != null) {
                               return objectLevelPermission(role, site.getString("associated_company_id"),
                                                            companyId).map(permitted -> {
                                   if (permitted) {
                                       return site;
                                   } else {
                                       throw HttpHelper.forbidden();
                                   }
                               });
                           } else {
                               throw HttpHelper.badRequest("Requested site doesn't exist on Database");
                           }
                       })
                       .flatMap(site -> {
                           String associatedCompanyId = body.getString("associated_company_id");
                           return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                                             .map(associatedCompany -> {
                                                 if (associatedCompany != null) {
                                                     return associatedCompany;
                                                 } else {
                                                     throw HttpHelper.badRequest(
                                                         "Requested company doesn't exist on Database.");
                                                 }
                                             })
                                             .flatMap(associatedCompany -> {
                                                 if (associatedCompany.getString("role")
                                                                      .equals(Role.MANAGER.toString())) {
                                                     if (role == Role.SUPER_ADMIN) {
                                                         return updateSite(body, associatedCompany.getString("_id"),
                                                                           site);
                                                     } else {
                                                         return byAdminCompanyGetManagerSelectionList(
                                                             companyId).flatMap(companies -> {
                                                             if (companies.contains(
                                                                 associatedCompany.getString("_id"))) {
                                                                 return updateSite(body,
                                                                                   associatedCompany.getString("_id"),
                                                                                   site);
                                                             } else {
                                                                 throw HttpHelper.forbidden();
                                                             }
                                                         });
                                                     }
                                                 } else {
                                                     throw HttpHelper.badRequest(
                                                         "You must associate Manager level company.");
                                                 }
                                             });
                       })
                       .subscribe(ignored -> message.reply(
                           new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                                  throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private SingleSource<?> updateSite(JsonObject body, String companyId, JsonObject site) {
        JsonObject siteObject = new Site(body.put("associated_company_id", companyId)).toJsonObject()
                                                                                      .put("role",
                                                                                           Role.MANAGER.toString())
                                                                                      .put("_id",
                                                                                           site.getString("_id"));
        return mongoClient.rxSave(SITE, siteObject);
    }

    private void handleUpdateUserGroup(Message<Object> message) {
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String userCompanyId = CustomMessageHelper.getCompanyId(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            Single.create(source -> {
                String userGroupId = CustomMessageHelper.getParamsId(message);
                mongoClient.rxFindOne(USER_GROUP, idQuery(userGroupId), null)
                           .map(userGroup -> {
                               if (userGroup != null) {
                                   return userGroup;
                               } else {
                                   throw HttpHelper.badRequest("User Group doesn't exist");
                               }
                           })
                           .flatMap(
                               userGroup -> objectLevelPermission(role, userGroup.getString("associated_company_id"),
                                                                  userCompanyId).map(permitted -> {
                                   if (permitted) {
                                       return userGroup;
                                   } else {
                                       throw HttpHelper.forbidden();
                                   }
                               }))
                           .flatMap(userGroup -> {
                               if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                                   return getManagerSiteQuery(role, userCompanyId).flatMap(
                                       managerSiteQuery -> mongoClient.rxFind(SITE, managerSiteQuery)
                                                                      .flatMap(childSitesResponse -> {
                                                                          if (childSitesResponse.size() > 0) {
                                                                              String[] availableSites
                                                                                  = MongoUtils.getIds(
                                                                                  childSitesResponse);
                                                                              String siteId = SQLUtils.getMatchValue(
                                                                                  body.getString("site_id", ""),
                                                                                  availableSites);
                                                                              if (siteId == null) {
                                                                                  throw HttpHelper.badRequest(
                                                                                      "Site doesn't match up " +
                                                                                      "Exception!");
                                                                              }
                                                                              String associatedCompanyId
                                                                                  = body.getString(
                                                                                  "associated_company_id", "");
                                                                              if (Strings.isNotBlank(
                                                                                  associatedCompanyId)) {
                                                                                  return mongoClient.rxFindOne(COMPANY,
                                                                                                               idQuery(
                                                                                                                   associatedCompanyId),
                                                                                                               null)
                                                                                                    .map(company -> {
                                                                                                        if (company !=
                                                                                                            null) {
                                                                                                            if (company.getString(
                                                                                                                "role")
                                                                                                                       .equals(
                                                                                                                           Role.MANAGER
                                                                                                                               .toString()) &&
                                                                                                                (role ==
                                                                                                                 Role.SUPER_ADMIN ||
                                                                                                                 (role ==
                                                                                                                  Role.ADMIN &&
                                                                                                                  managerSiteQuery
                                                                                                                      .getJsonObject(
                                                                                                                          "associated_company_id")
                                                                                                                      .getJsonArray(
                                                                                                                          "$in")
                                                                                                                      .contains(
                                                                                                                          associatedCompanyId)))) {
                                                                                                                return new UserGroup(
                                                                                                                    body.put(
                                                                                                                        "associated_company_id",
                                                                                                                        associatedCompanyId)
                                                                                                                        .put(
                                                                                                                            "role",
                                                                                                                            Role.USER
                                                                                                                                .toString())
                                                                                                                        .put(
                                                                                                                            "site_id",
                                                                                                                            siteId))
                                                                                                                           .toJsonObject()
                                                                                                                           .put(
                                                                                                                               "_id",
                                                                                                                               userGroup
                                                                                                                                   .getString(
                                                                                                                                       "_id"));
                                                                                                            } else {
                                                                                                                throw HttpHelper
                                                                                                                          .badRequest(
                                                                                                                              "We should assign Manager level company");
                                                                                                            }
                                                                                                        } else {
                                                                                                            throw HttpHelper
                                                                                                                      .badRequest(
                                                                                                                          "We don't have the associated_company_id.");
                                                                                                        }
                                                                                                    });
                                                                              } else {
                                                                                  throw HttpHelper.badRequest(
                                                                                      "No associated_company_id value" +
                                                                                      " is requested.");
                                                                              }
                                                                          } else {
                                                                              throw HttpHelper.badRequest(
                                                                                  "Create <Site> at first.");
                                                                          }
                                                                      }));
                               } else {
                                   return Single.just(new UserGroup(body.put("associated_company_id", userCompanyId)
                                                                        .put("site_id", CustomMessageHelper.getSiteId(
                                                                            user))).toJsonObject()
                                                                                   .put("_id",
                                                                                        userGroup.getString("_id")));
                               }
                           })
                           .subscribe(source::onSuccess, source::onError);
            })
                  .flatMap(userGroup -> mongoClient.rxSave(USER_GROUP, (JsonObject) userGroup))
                  .subscribe(ignore -> message.reply(
                      new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                             throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleUpdateCompany(Message<Object> message) {
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            String companyId = CustomMessageHelper.getParamsId(message);
            Single.create(source -> {
                if (role == Role.SUPER_ADMIN) {
                    mongoClient.rxFindOne(COMPANY, idQuery(companyId), null).flatMap(company -> {
                        if (company != null) {
                            if (company.getString("role").equals(Role.ADMIN.toString())) {
                                return Single.just(new Company(body.put("role", company.getString("role"))
                                                                   .put("associated_company_id",
                                                                        company.getString("associated_company_id"))));
                            } else {
                                return mongoClient.rxFindOne(COMPANY,
                                                             idQuery(body.getString("associated_company_id", "")), null)
                                                  .map(associatedCompany -> {
                                                      if (associatedCompany != null && UserUtils.getRole(
                                                          Role.valueOf(associatedCompany.getString("role")))
                                                                                                .toString()
                                                                                                .equals(
                                                                                                    company.getString(
                                                                                                        "role"))) {

                                                          return new Company(
                                                              body.put("role", company.getString("role")));
                                                      } else {
                                                          throw HttpHelper.badRequest(
                                                              "You can't associated that <Company>!");
                                                      }
                                                  });
                            }
                        } else {
                            throw HttpHelper.badRequest("Requested <Company> doesn't exist!");
                        }
                    }).subscribe(source::onSuccess, source::onError);
                } else {
                    mongoClient.rxFindOne(COMPANY, idQuery(companyId), null).map(company -> {
                        if (company != null && company.getString("role").equals(Role.MANAGER.toString()) &&
                            company.getString("associated_company_id").equals(CustomMessageHelper.getCompanyId(user))) {

                            return new Company(body.put("role", company.getString("role"))
                                                   .put("associated_company_id",
                                                        company.getString("associated_company_id")));
                        } else {
                            throw HttpHelper.forbidden();
                        }
                    }).subscribe(source::onSuccess, source::onError);
                }
            })
                  .flatMap(
                      company -> mongoClient.rxSave(COMPANY, ((Company) company).toJsonObject().put("_id", companyId)))
                  .subscribe(ignore -> message.reply(
                      new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                             throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleChangeDefaultSite(Message<Object> message) {
        JsonObject body = CustomMessageHelper.getBodyAsJson(message);
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);

        if (role == Role.MANAGER) {
            String siteId = body.getString("site_id");
            if (user.getJsonArray("sites_ids").contains(siteId)) {
                mongoClient.rxFindOne(USER, idQuery(user.getString("_id")), null)
                           .flatMap(respondUser -> mongoClient.rxSave(USER, respondUser.put("site_id", siteId)))
                           .subscribe(ignore -> message.reply(
                               new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.NO_CONTENT.code())),
                                      throwable -> handleHttpException(message, throwable));
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePutSite(Message<Object> message) {
        // This will call on the first time site initialization and update site
        JsonObject user = CustomMessageHelper.getUser(message);
        Role role = CustomMessageHelper.getRole(user);
        String companyId = CustomMessageHelper.getCompanyId(user);
        String siteId = CustomMessageHelper.getHeaders(message).getString("Site-Id");
        JsonObject headers = CustomMessageHelper.getHeaders(message);

        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            Single.create(source -> {
                if (role != Role.MANAGER) {
                    // Create or Update by SUPER_ADMIN/ADMIN
                    JsonObject query = new JsonObject().put("associated_company_id", companyId);
                    mongoClient.rxFind(SITE, query).flatMap(respondSites -> {
                        Site site = new Site(CustomMessageHelper.getBodyAsJson(message)
                                                                .put("associated_company_id", companyId)
                                                                .put("role", role.toString()));
                        if (respondSites.size() > 0) {
                            return mongoClient.rxSave(SITE, site.toJsonObject()
                                                                .put("_id", respondSites.get(0).getString("_id")));
                        } else {
                            // First time site initialization
                            return mongoClient.rxSave(SITE, site.toJsonObject()).flatMap(siteId$ -> {
                                // Updating site_id for all Users which are associated to that Site
                                return mongoClient.rxUpdateCollectionWithOptions(USER,
                                                                                 new JsonObject().put("company_id",
                                                                                                      companyId),
                                                                                 new JsonObject().put("$set",
                                                                                                      new JsonObject().put(
                                                                                                          "site_id",
                                                                                                          siteId$)),
                                                                                 new UpdateOptions(false, true))
                                                  .flatMap(ignored -> {
                                                      if (appConfig.getBoolean("ditto-policy")) {
                                                          if (role == Role.SUPER_ADMIN) {
                                                              return mongoClient.rxFind(USER,
                                                                                        new JsonObject().put("role",
                                                                                                             Role.SUPER_ADMIN
                                                                                                                 .toString()))
                                                                                .flatMap(users -> dispatchRequests(
                                                                                    microContext, HttpMethod.PUT,
                                                                                    headers,
                                                                                    Services.POLICY_PREFIX + siteId$,
                                                                                    DittoUtils.createPolicy(users)));
                                                          } else {
                                                              return mongoClient.rxFind(USER,
                                                                                        new JsonObject().put("$or",
                                                                                                             new JsonArray()
                                                                                                                 .add(
                                                                                                                     new JsonObject()
                                                                                                                         .put(
                                                                                                                             "role",
                                                                                                                             Role.SUPER_ADMIN
                                                                                                                                 .toString()))
                                                                                                                 .add(
                                                                                                                     new JsonObject()
                                                                                                                         .put(
                                                                                                                             "company_id",
                                                                                                                             companyId))))

                                                                                .flatMap(users -> dispatchRequests(
                                                                                    microContext, HttpMethod.PUT,
                                                                                    headers,
                                                                                    Services.POLICY_PREFIX + siteId$,
                                                                                    DittoUtils.createPolicy(users)));
                                                          }
                                                      } else {
                                                          return Single.just("");
                                                      }
                                                  });
                            });
                        }
                    }).subscribe(ignore -> source.onSuccess(""), source::onError);
                } else {
                    // Update Site by MANAGER
                    JsonObject query = new JsonObject().put("_id", siteId);
                    mongoClient.rxFindOne(SITE, query, null).flatMap(respondSite -> {
                        if (respondSite != null) {
                            if (respondSite.getString("associated_company_id").equals(companyId)) {
                                Site site = new Site(CustomMessageHelper.getBodyAsJson(message)
                                                                        .put("associated_company_id",
                                                                             respondSite.getString(
                                                                                 "associated_company_id"))
                                                                        .put("role", respondSite.getString("role")));
                                return mongoClient.rxSave(SITE, site.toJsonObject().put("_id", siteId));
                            } else {
                                throw HttpHelper.forbidden();
                            }
                        } else {
                            throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "<Site> doesn't exist!");
                        }
                    }).subscribe(ignore -> source.onSuccess(""), source::onError);
                }
            })
                  .subscribe(ignored -> message.reply(
                      new CustomMessage<>(null, new JsonObject(), HttpResponseStatus.OK.code())),
                             throwable -> handleHttpException(message, throwable));
        } else {
            handleForbiddenResponse(message);
        }
    }

    private Single<JsonObject> associatedCompanyRepresentation(JsonObject object, String associatedCompanyId) {
        if (Strings.isNotBlank(associatedCompanyId)) {
            return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null).map(associatedCompany -> {
                if (associatedCompany != null) {
                    return object.put("associated_company", associatedCompany);
                }
                return object;
            });
        } else {
            return Single.just(object);
        }
    }

    private void respondRequestWithCompanyAssociateCompanyGroupAndSiteRepresentation(Message<Object> message,
                                                                                     JsonObject query,
                                                                                     String collection) {
        // We may do optimize version of this
        mongoClient.rxFind(collection, query)
                   .flatMap(response -> Observable.fromIterable(response).flatMapSingle(res -> {
                       JsonObject object = new JsonObject(res.toString());
                       return mongoClient.rxFindOne(COMPANY, idQuery(object.getString("associated_company_id")), null)
                                         .flatMap(associatedCompany -> {
                                             if (associatedCompany != null) {
                                                 object.put("associated_company", associatedCompany);
                                             }
                                             return mongoClient.rxFindOne(COMPANY,
                                                                          idQuery(object.getString("company_id")), null)
                                                               .flatMap(company -> {
                                                                   if (company != null) {
                                                                       object.put("company", company);
                                                                   }
                                                                   if (Strings.isNotBlank(
                                                                       object.getString("group_id"))) {
                                                                       return mongoClient.rxFindOne(USER_GROUP, idQuery(
                                                                           object.getString("group_id")), null)
                                                                                         .flatMap(group -> {
                                                                                             if (group != null) {
                                                                                                 object.put("group",
                                                                                                            group);
                                                                                             }
                                                                                             return buildSite(message,
                                                                                                              object);
                                                                                         });
                                                                   } else {
                                                                       return buildSite(message, object);
                                                                   }
                                                               });
                                         });
                   }).toList())
                   .subscribe(response -> {
                       JsonArray array = new JsonArray();
                       response.forEach(array::add);
                       message.reply(new CustomMessage<>(null, array, HttpResponseStatus.OK.code()));
                   }, throwable -> handleHttpException(message, throwable));
    }

    private SingleSource<? extends JsonObject> buildSite(Message<Object> message, JsonObject object) {
        if (Strings.isNotBlank(object.getString("site_id"))) {
            return mongoClient.rxFindOne(SITE, idQuery(object.getString("site_id")), null).map(site -> {
                if (site != null) {
                    object.put("site", site);
                }
                return object;
            });
        } else {
            return Single.just(object);
        }
    }

    private Single<JsonObject> byAdminCompanyGetManagerSelectionListQuery(String companyId) {
        return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId)
                                                           .put("role", Role.MANAGER.toString()))
                          .map(response -> new JsonObject().put("associated_company_id", new JsonObject().put("$in",
                                                                                                              MongoUtils
                                                                                                                  .getIdsOnJsonArray(
                                                                                                                      response))));
    }

    private Single<List<String>> byAdminCompanyGetManagerSelectionList(String companyId) {
        return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId)
                                                           .put("role", Role.MANAGER.toString()))
                          .map(MongoUtils::getIdsOnList);
    }

    private Single<JsonObject> byAdminCompanyGetAdminWithManagerSelectionListQuery(String companyId) {
        return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId)
                                                           .put("role", Role.MANAGER.toString()))
                          .map(response -> new JsonObject().put("associated_company_id", new JsonObject().put("$in",
                                                                                                              MongoUtils
                                                                                                                  .getIdsOnJsonArray(
                                                                                                                      response)
                                                                                                                  .add(
                                                                                                                      companyId))));
    }

    private Single<List<String>> byAdminCompanyGetAdminWithManagerSelectionList(String companyId) {
        return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId)
                                                           .put("role", Role.MANAGER.toString())).map(response -> {
            List<String> companies = MongoUtils.getIdsOnList(response);
            companies.add(companyId);
            return companies;
        });
    }

    private Single<JsonObject> getManagerSiteQuery(Role role, String userCompanyId) {
        if (role == Role.SUPER_ADMIN) {
            return Single.just(new JsonObject().put("role", Role.MANAGER));
        } else {
            return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", userCompanyId)
                                                               .put("role", Role.MANAGER.toString()))
                              .map(response -> new JsonObject().put("associated_company_id", new JsonObject().put("$in",
                                                                                                                  MongoUtils
                                                                                                                      .getIdsOnJsonArray(
                                                                                                                          response))));
        }
    }

    private Single<Boolean> objectLevelPermission(Role role, String toCheckCompanyId, String companyId) {
        if (role == Role.SUPER_ADMIN) {
            return Single.just(true);
        } else if (role == Role.ADMIN) {
            return byAdminCompanyGetAdminWithManagerSelectionList(companyId).map(
                list -> list.contains(toCheckCompanyId));
        } else if (role == Role.MANAGER) {
            return Single.just(companyId.equals(toCheckCompanyId));
        }
        return Single.just(false);
    }

}
