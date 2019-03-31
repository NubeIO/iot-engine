package com.nubeiot.dashboard.controllers;

import static com.nubeiot.core.mongo.MongoUtils.idQuery;
import static com.nubeiot.dashboard.constants.Collection.COMPANY;
import static com.nubeiot.dashboard.constants.Collection.SITE;
import static com.nubeiot.dashboard.constants.Collection.USER_GROUP;
import static com.nubeiot.dashboard.helpers.MultiTenantPermissionHelper.checkPermissionAndReturnValue;
import static com.nubeiot.dashboard.helpers.MultiTenantPermissionHelper.nullableCheck;
import static com.nubeiot.dashboard.helpers.MultiTenantPermissionHelper.objectLevelPermission;
import static com.nubeiot.dashboard.helpers.MultiTenantQueryBuilderHelper.byAdminCompanyGetManagerSelectionListQuery;
import static com.nubeiot.dashboard.helpers.MultiTenantQueryBuilderHelper.getManagerSiteQuery;
import static com.nubeiot.dashboard.helpers.MultiTenantRepresentationHelper.associatedCompanyRepresentation;
import static com.nubeiot.dashboard.utils.UserUtils.getCompanyId;
import static com.nubeiot.dashboard.utils.UserUtils.getRole;
import static com.nubeiot.dashboard.utils.UserUtils.getSiteId;

import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.mongo.MongoUtils;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.core.utils.SQLUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.models.UserGroup;
import com.zandero.rest.annotation.RouteOrder;

@Path("/api")
public class MultiTenantUserGroupController implements RestApi {

    @GET
    @Path("/user_groups")
    @RouteOrder(3)
    public Future<ResponseData> get(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleGetUserGroups(ctx, mongoClient.getMongoClient());
    }

    @POST
    @Path("/user_group")
    @RouteOrder(3)
    public Future<ResponseData> post(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handlePostUserGroup(ctx, mongoClient.getMongoClient());
    }

    @PATCH
    @Path("/user_group/:id")
    @RouteOrder(3)
    public Future<ResponseData> patch(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleUpdateUserGroup(ctx, mongoClient.getMongoClient());
    }

    @POST
    @Path("/delete_user_groups")
    @RouteOrder(3)
    public Future<ResponseData> delete(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleDeleteUserGroups(ctx, mongoClient.getMongoClient());
    }

    private Future<ResponseData> handleUpdateUserGroup(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject body = ctx.getBodyAsJson();
        JsonObject user = ctx.user().principal();
        String companyId = getCompanyId(user);

        Single.just(getRole(user))
            .flatMap(this::userRolePermissionCheck)
            .flatMap(role -> {
                String userGroupId = ctx.request().getParam("id");
                return mongoClient.rxFindOne(USER_GROUP, idQuery(userGroupId), null)
                    .map(userGroup -> nullableCheck(userGroup, "User Group doesn't exist"))
                    .flatMap(
                        userGroup -> objectLevelPermission(mongoClient, userGroup.getString("associated_company_id"),
                                                           role, companyId).map(ignored -> userGroup))
                    .flatMap(userGroup -> {
                        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                            return editUserGroup(mongoClient, body, companyId, role)
                                .map(body$ -> new UserGroup(body$).toJsonObject()
                                    .put("_id", userGroup.getString("_id")));
                        } else {
                            return Single.just(new UserGroup(
                                body.put("associated_company_id", companyId).put("site_id", getSiteId(user)))
                                                   .toJsonObject().put("_id", userGroup.getString("_id")));
                        }
                    });
            })
            .flatMap(userGroup -> mongoClient.rxSave(USER_GROUP, userGroup))
            .subscribe(
                userGroups -> future.complete(new ResponseData().setStatusCode(HttpResponseStatus.NO_CONTENT.code())),
                throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Future<ResponseData> handleGetUserGroups(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        String companyId = getCompanyId(user);

        Single.just(getRole(user))
            .flatMap(role -> {
                if (role == Role.SUPER_ADMIN) {
                    return mongoClient.rxFind(USER_GROUP, new JsonObject());
                } else if (role == Role.ADMIN) {
                    return byAdminCompanyGetManagerSelectionListQuery(mongoClient, companyId).flatMap(
                        query -> mongoClient.rxFind(USER_GROUP, query));
                } else if (role == Role.MANAGER) {
                    return mongoClient.rxFind(USER_GROUP, new JsonObject().put("associated_company_id", companyId));
                } else {
                    return mongoClient.rxFind(USER_GROUP, new JsonObject().put("_id", user.getString("group_id")));
                }
            })
            .flatMap(userGroups -> Observable.fromIterable(userGroups)
                .flatMapSingle(userGroup -> mongoClient.rxFindOne(SITE, idQuery(userGroup.getString("site_id")), null)
                    .flatMap(site -> associatedCompanyRepresentation(mongoClient, userGroup).map(ignored -> {
                        if (site != null) {
                            return userGroup.put("site", site);
                        }
                        return userGroup;
                    })))
                .toList())
            .subscribe(userGroups -> future.complete(new ResponseData().setBodyMessage(userGroups.toString())),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Future<ResponseData> handlePostUserGroup(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject body = ctx.getBodyAsJson();
        JsonObject user = ctx.user().principal();
        String companyId = getCompanyId(user);

        Single.just(getRole(user))
            .flatMap(this::userRolePermissionCheck)
            .flatMap(role -> {
                if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
                    return editUserGroup(mongoClient, body, companyId, role).map(UserGroup::new);
                } else {
                    return Single.just(
                        new UserGroup(body.put("associated_company_id", companyId).put("site_id", getSiteId(user))));
                }
            })
            .flatMap(userGroup -> mongoClient.rxSave(USER_GROUP, userGroup.toJsonObject()))
            .subscribe(
                userGroups -> future.complete(new ResponseData().setStatusCode(HttpResponseStatus.CREATED.code())),
                throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Single<JsonObject> editUserGroup(MongoClient mongoClient, JsonObject body, String companyId, Role role) {
        return getManagerSiteQuery(mongoClient, role, companyId).flatMap(
            managerSiteQuery -> mongoClient.rxFind(SITE, managerSiteQuery).flatMap(childSitesResponse -> {
                if (childSitesResponse.size() > 0) {
                    String[] availableSites = MongoUtils.getIds(childSitesResponse);
                    String siteId = SQLUtils.getMatchValue(body.getString("site_id", ""), availableSites);
                    if (siteId == null) {
                        throw HttpException.badRequest("Site doesn't match up Exception!");
                    }
                    String associatedCompanyId = body.getString("associated_company_id", "");
                    if (Strings.isNotBlank(associatedCompanyId)) {
                        return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                            .map(company -> {
                                if (company != null) {
                                    if (company.getString("role").equals(Role.MANAGER.toString()) &&
                                        (role == Role.SUPER_ADMIN || (role == Role.ADMIN && managerSiteQuery
                                            .getJsonObject("associated_company_id").getJsonArray("$in")
                                            .contains(associatedCompanyId)))) {
                                        return body.put("associated_company_id", associatedCompanyId)
                                            .put("site_id", siteId);
                                    } else {
                                        throw HttpException.badRequest("We should assign Manager level company.");
                                    }
                                } else {
                                    throw HttpException.badRequest("We don't have the associated_company_id.");
                                }
                            });
                    } else {
                        throw HttpException.badRequest("No associated_company_id value is requested.");
                    }
                } else {
                    throw HttpException.badRequest("Create <Site> at first.");
                }
            }));
    }

    private Single<Role> userRolePermissionCheck(Role role) {
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
            return Single.just(role);
        } else {
            throw HttpException.forbidden();
        }
    }

    private Future<ResponseData> handleDeleteUserGroups(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        String companyId = getCompanyId(user);

        Single.just(getRole(user))
            .flatMap(this::userRolePermissionCheck)
            .flatMap(role -> {
                JsonArray queryInput = ctx.getBodyAsJsonArray();
                // Object level permission
                JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
                return mongoClient.rxFind(USER_GROUP, query)
                    .flatMap(userGroups -> {
                        if (userGroups.size() == queryInput.size()) {
                            return checkPermissionAndReturnValue(mongoClient, companyId, role, userGroups);
                        } else {
                            throw HttpException.badRequest("Doesn't have those <User Groups> on Database.");
                        }
                    }).flatMap(ignored -> mongoClient.rxRemoveDocuments(USER_GROUP, query));
            })
            .subscribe(
                userGroups -> future.complete(new ResponseData().setStatusCode(HttpResponseStatus.NO_CONTENT.code())),
                throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

}
