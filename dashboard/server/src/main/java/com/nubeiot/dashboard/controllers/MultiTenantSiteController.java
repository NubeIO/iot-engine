package com.nubeiot.dashboard.controllers;

import static com.nubeiot.core.mongo.MongoUtils.idQuery;
import static com.nubeiot.dashboard.constants.Collection.COMPANY;
import static com.nubeiot.dashboard.constants.Collection.SITE;
import static com.nubeiot.dashboard.constants.Collection.USER;
import static com.nubeiot.dashboard.helpers.MultiTenantPermissionHelper.checkPermissionAndReturnValue;
import static com.nubeiot.dashboard.helpers.MultiTenantPermissionHelper.nullableCheck;
import static com.nubeiot.dashboard.helpers.MultiTenantPermissionHelper.objectLevelPermission;
import static com.nubeiot.dashboard.helpers.MultiTenantQueryBuilderHelper.byAdminCompanyGetManagerSelectionList;
import static com.nubeiot.dashboard.helpers.MultiTenantQueryBuilderHelper.byAdminCompanyGetManagerSelectionListQuery;
import static com.nubeiot.dashboard.helpers.MultiTenantRepresentationHelper.associatedCompanyRepresentation;
import static com.nubeiot.dashboard.utils.DispatchUtils.dispatchRequests;
import static com.nubeiot.dashboard.utils.UserUtils.getCompanyId;
import static com.nubeiot.dashboard.utils.UserUtils.getRole;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.http.converter.RequestDataConverter;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestConfigProvider;
import com.nubeiot.core.http.rest.provider.RestMicroContextProvider;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.dashboard.DashboardServerConfig;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.constants.Services;
import com.nubeiot.dashboard.models.Site;
import com.nubeiot.dashboard.props.SiteProps;
import com.nubeiot.dashboard.utils.DittoUtils;
import com.zandero.rest.annotation.RouteOrder;

public class MultiTenantSiteController implements RestApi {

    @GET
    @Path("/sites")
    @RouteOrder(3)
    public Future<ResponseData> get(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleGetSites(ctx, mongoClient.getMongoClient());
    }

    @POST
    @Path("/site")
    @RouteOrder(3)
    public Future<ResponseData> post(@Context RestMicroContextProvider microContextProvider,
                                     @Context RoutingContext ctx,
                                     @Context RestConfigProvider configProvider,
                                     @Context RestMongoClientProvider mongoClient) {
        DashboardServerConfig dashboardServerConfig = IConfig.from(configProvider.getConfig().getAppConfig(),
                                                                   DashboardServerConfig.class);
        return handlePostSite(microContextProvider.getMicroContext(), ctx, mongoClient.getMongoClient(),
                              dashboardServerConfig);
    }

    @PUT
    @Path("/site")
    @RouteOrder(3)
    public Future<ResponseData> put(@Context RestMicroContextProvider microContextProvider,
                                    @Context RoutingContext ctx,
                                    @Context RestConfigProvider configProvider,
                                    @Context RestMongoClientProvider mongoClient) {
        DashboardServerConfig dashboardServerConfig = IConfig.from(configProvider.getConfig().getAppConfig(),
                                                                   DashboardServerConfig.class);
        return handlePutSite(microContextProvider.getMicroContext(), ctx, mongoClient.getMongoClient(),
                             dashboardServerConfig);
    }

    @PATCH
    @Path("/site/:id")
    @RouteOrder(3)
    public Future<ResponseData> patch(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleUpdateSite(ctx, mongoClient.getMongoClient());
    }

    @POST
    @Path("/delete_sites")
    @RouteOrder(3)
    public Future<ResponseData> delete(@Context RestMicroContextProvider microContextProvider,
                                       @Context RoutingContext ctx,
                                       @Context RestConfigProvider configProvider,
                                       @Context RestMongoClientProvider mongoClient) {
        DashboardServerConfig dashboardServerConfig = IConfig.from(configProvider.getConfig().getAppConfig(),
                                                                   DashboardServerConfig.class);
        return handleDeleteSites(microContextProvider.getMicroContext(), ctx, mongoClient.getMongoClient(),
                                 dashboardServerConfig);
    }

    @PATCH
    @Path("/change_default_site")
    @RouteOrder(3)
    public Future<ResponseData> changeDefaultSite(@Context RoutingContext ctx,
                                                  @Context RestMongoClientProvider mongoClient) {
        return handleChangeDefaultSite(ctx, mongoClient.getMongoClient());
    }

    private Future<ResponseData> handleChangeDefaultSite(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject body = ctx.getBodyAsJson();
        JsonObject user = ctx.user().principal();

        Single.just(getRole(user)).flatMap(role -> {
            if (role == Role.MANAGER) {
                String siteId = body.getString("site_id");
                if (user.getJsonArray("sites_ids").contains(siteId)) {
                    return mongoClient.rxFindOne(USER, idQuery(user.getString("_id")), null)
                        .flatMap(respondUser -> mongoClient.rxSave(USER, respondUser.put("site_id", siteId)));
                } else {
                    throw HttpException.forbidden();
                }
            } else {
                throw HttpException.forbidden();
            }
        }).subscribe(ignored -> future.complete(new ResponseData().setStatus(HttpResponseStatus.NO_CONTENT.code())),
                     throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Future<ResponseData> handleDeleteSites(MicroContext microContext, RoutingContext ctx,
                                                   MongoClient mongoClient,
                                                   DashboardServerConfig dashboardServerConfig) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();

        SiteProps siteProps = SiteProps.builder()
            .microContext(microContext)
            .routingContext(ctx)
            .mongoClient(mongoClient)
            .dashboardServerConfig(dashboardServerConfig)
            .arrayBody(ctx.getBodyAsJsonArray())
            .user(user)
            .companyId(getCompanyId(user))
            .siteId(ctx.request().getHeader("Site-Id"))
            .role(getRole(user))
            .build();

        Single.just(siteProps.getRole()).flatMap(this::userRolePermissionCheck)
            .flatMap(ignored -> {
                // Object level permission
                JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", siteProps.getArrayBody()));
                return mongoClient.rxFind(SITE, query).flatMap(sites -> {
                    if (sites.size() == siteProps.getArrayBody().size()) {
                        return checkPermissionAndReturnValue(siteProps.getMongoClient(), siteProps.getCompanyId(),
                                                             siteProps.getRole(), sites);
                    } else {
                        throw HttpException.badRequest("Doesn't have those <Sites> on Database.");
                    }
                }).flatMap(ignored$ -> Observable.fromIterable(siteProps.getArrayBody())
                    .flatMapSingle(id -> mongoClient.rxRemoveDocument(SITE, idQuery(id.toString())).flatMap(ign -> {
                        // TODO: to make dispatchRequests working
                        if (siteProps.getDashboardServerConfig().getDittoPolicy()) {
                            return dispatchRequests(microContext, HttpMethod.DELETE, Services.POLICY_PREFIX + id, null);
                        } else {
                            return Single.just("");
                        }
                    }))
                    .toList());
            })
            .subscribe(ignored -> future.complete(new ResponseData().setStatus(HttpResponseStatus.NO_CONTENT.code())),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    /**
     * Only Manager's {@code Site} is editable by {@code SUPER_ADMIN} or its parent {@code ADMIN}
     */
    private Future<ResponseData> handleUpdateSite(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        SiteProps siteProps = SiteProps.builder()
            .mongoClient(mongoClient)
            .body(ctx.getBodyAsJson())
            .user(user)
            .role(getRole(user))
            .companyId(getCompanyId(user))
            .siteId(ctx.request().getParam("id"))
            .build();

        Single.just(siteProps.getRole())
            .flatMap(this::userRolePermissionCheck)
            .flatMap(ignored -> userUpdatePermissionCheck(siteProps))
            .flatMap(ignored -> associatedCompanyOfSiteHasRoleManagerCheck(siteProps))
            .flatMap(ignored -> requestedSiteIsAssociateWithChildCompanyCheck(siteProps))
            .flatMap(ignored -> saveUpdatedSite(siteProps))
            .subscribe(ignored -> future.complete(new ResponseData()),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Single<Boolean> userUpdatePermissionCheck(SiteProps siteProps) {
        return siteProps.getMongoClient().rxFindOne(SITE, idQuery(siteProps.getSiteId()), null)
            .map(site -> nullableCheck(site, "Requested site doesn't exist on Database."))
            .flatMap(site -> {
                siteProps.setSite(site);
                return objectLevelPermission(siteProps.getMongoClient(), siteProps.getCompanyId(), siteProps.getRole(),
                                             site.getString("associated_company_id"));
            });
    }

    private Single<Boolean> associatedCompanyOfSiteHasRoleManagerCheck(SiteProps siteProps) {
        System.out.println("Body is: " + siteProps.getBody());
        String associatedCompanyId = siteProps.getBody().getString("associated_company_id");
        return siteProps.getMongoClient().rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
            .map(
                associatedCompany -> nullableCheck(associatedCompany, "Requested company doesn't exist on Database."))
            .map(associatedCompany -> {
                siteProps.setAssociatedCompanyId(associatedCompany.getString("_id"));
                if (associatedCompany.getString("role").equals(Role.MANAGER.toString())) {
                    return true;
                } else {
                    throw HttpException.badRequest("You must associate Manager level company.");
                }
            });
    }

    private Single<Boolean> requestedSiteIsAssociateWithChildCompanyCheck(SiteProps siteProps) {
        if (siteProps.getRole() == Role.SUPER_ADMIN) {
            return Single.just(true);
        } else {
            return byAdminCompanyGetManagerSelectionList(siteProps.getMongoClient(), siteProps.getCompanyId())
                .map(companies -> {
                    if (companies.contains(siteProps.getBody().getString("associated_company_id"))) {
                        return true;
                    } else {
                        throw HttpException.forbidden();
                    }
                });
        }
    }

    private Single<String> saveUpdatedSite(SiteProps siteProps) {
        JsonObject siteObject =
            new Site(siteProps.getBody().put("associated_company_id", siteProps.getAssociatedCompanyId()))
                .toJsonObject()
                .put("role", Role.MANAGER.toString())
                .put("_id", siteProps.getSite().getString("_id"));
        return siteProps.getMongoClient().rxSave(SITE, siteObject);
    }

    /**
     * This will call on the {@code Site} initialization for those {@code Company} having role {@code SUPER_ADMIN/ADMIN}
     * and update own {@code Site} having {@code Role} {@code SUPER_ADMIN/ADMIN/MANAGER}
     */
    private Future<ResponseData> handlePutSite(MicroContext microContext, RoutingContext ctx, MongoClient mongoClient,
                                               DashboardServerConfig dashboardServerConfig) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();

        SiteProps siteProps = SiteProps.builder()
            .microContext(microContext)
            .routingContext(ctx)
            .mongoClient(mongoClient)
            .dashboardServerConfig(dashboardServerConfig)
            .body(ctx.getBodyAsJson())
            .user(user)
            .companyId(getCompanyId(user))
            .siteId(ctx.request().getHeader("Site-Id"))
            .role(getRole(user))
            .build();

        Single
            .just(siteProps.getRole())
            .flatMap(this::userRolePermissionCheckForPut)
            .flatMap(role -> {
                if (role == Role.MANAGER) {
                    return updateOwnManagerSite(siteProps);
                } else {
                    return createOrUpdateOwnSite(siteProps);
                }
            })
            .subscribe(ignored -> future.complete(new ResponseData()),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Single<String> updateOwnManagerSite(SiteProps siteProps) {
        // Update own Site by MANAGER
        JsonObject query = new JsonObject().put("_id", siteProps.getSiteId());
        return siteProps.getMongoClient().rxFindOne(SITE, query, null).flatMap(respondSite -> {
            if (respondSite != null) {
                if (respondSite.getString("associated_company_id").equals(siteProps.getCompanyId())) {
                    JsonObject body = siteProps.getBody()
                        .put("associated_company_id", respondSite.getString("associated_company_id"))
                        .put("role", respondSite.getString("role"));
                    Site site = new Site(body);
                    return siteProps.getMongoClient()
                        .rxSave(SITE, site.toJsonObject().put("_id", siteProps.getSiteId()));
                } else {
                    throw HttpException.forbidden();
                }
            } else {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "<Site> doesn't exist!");
            }
        });
    }

    private Single<String> createOrUpdateOwnSite(SiteProps siteProps) {
        // Create or Update own Site by not MANAGER (i.e. by SUPER_ADMIN or by ADMIN)
        JsonObject query = new JsonObject().put("associated_company_id", siteProps.getCompanyId());
        return siteProps.getMongoClient().rxFind(SITE, query).flatMap(respondSites -> {
            Site site = new Site(
                siteProps.getBody().put("associated_company_id", siteProps.getCompanyId())
                    .put("role", siteProps.getRole().toString()));
            if (respondSites.size() > 0) {
                return siteProps.getMongoClient()
                    .rxSave(SITE, site.toJsonObject().put("_id", respondSites.get(0).getString("_id")));
            } else {
                siteProps.setSite(site.toJsonObject());
                return firstTimeInitialization(siteProps).map(ignored -> "");
            }
        });
    }

    private Single<Boolean> firstTimeInitialization(SiteProps siteProps) {
        // First time site initialization
        return siteProps.getMongoClient().rxSave(SITE, siteProps.getSite()).flatMap(siteId -> {
            // Updating site_id for all Users which are associated to that Site
            siteProps.setSiteId(siteId);
            JsonObject query = new JsonObject().put("company_id", siteProps.getCompanyId());
            JsonObject update = new JsonObject().put("$set", new JsonObject().put("site_id", siteId));
            return siteProps.getMongoClient()
                .rxUpdateCollectionWithOptions(USER, query, update, new UpdateOptions(false, true))
                .flatMap(ignored -> {
                    if (siteProps.getDashboardServerConfig().getDittoPolicy()) {
                        return putDittoPolicy(siteProps).map(ignored$ -> true);
                    } else {
                        return Single.just(true);
                    }
                });
        });
    }

    private Single<ResponseData> putDittoPolicy(SiteProps siteProps) {
        if (siteProps.getRole() == Role.SUPER_ADMIN) {
            return siteProps.getMongoClient().rxFind(USER, new JsonObject().put("role", Role.SUPER_ADMIN.toString()))
                .flatMap(users -> dispatchDittoPolicy(siteProps, users));
        } else {
            JsonObject findQuery = new JsonObject().put("$or", new JsonArray()
                .add(new JsonObject().put("role", Role.SUPER_ADMIN.toString()))
                .add(new JsonObject().put("company_id", siteProps.getCompanyId())));
            return siteProps.getMongoClient()
                .rxFind(USER, findQuery)
                .flatMap(users -> dispatchDittoPolicy(siteProps, users));
        }
    }

    // TODO: to make dispatchRequests working
    private Single<ResponseData> dispatchDittoPolicy(SiteProps siteProps, List<JsonObject> users) {
        RequestData requestData = RequestDataConverter.convert(siteProps.getRoutingContext());
        requestData.setBody(DittoUtils.createPolicy(users));
        return dispatchRequests(siteProps.getMicroContext(), HttpMethod.PUT,
                                Services.POLICY_PREFIX + siteProps.getSiteId(), requestData);
    }

    private Single<Role> userRolePermissionCheckForPut(Role role) {
        if (role == Role.SUPER_ADMIN || role == Role.ADMIN || role == Role.MANAGER) {
            return Single.just(role);
        } else {
            throw HttpException.forbidden();
        }
    }

    private Future<ResponseData> handleGetSites(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        String companyId = getCompanyId(user);

        Single
            .just(getRole(user))
            .flatMap(role -> {
                if (role == Role.SUPER_ADMIN) {
                    return mongoClient.rxFind(SITE, new JsonObject().put("role", Role.MANAGER.toString()));
                } else if (role == Role.ADMIN) {
                    return byAdminCompanyGetManagerSelectionListQuery(mongoClient, companyId).flatMap(
                        query -> mongoClient.rxFind(SITE, query));
                } else if (role == Role.MANAGER) {
                    return mongoClient.rxFind(SITE, new JsonObject().put("associated_company_id", companyId));
                } else {
                    throw HttpException.forbidden();
                }
            })
            .flatMap(sites -> Observable
                .fromIterable(sites)
                .flatMapSingle(site -> associatedCompanyRepresentation(mongoClient, site))
                .toList())
            .subscribe(sites -> future.complete(ResponseDataWriter.serializeResponseData(sites.toString())),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));

        return future;
    }

    private Future<ResponseData> handlePostSite(MicroContext microContext, RoutingContext ctx,
                                                MongoClient mongoClient, DashboardServerConfig dashboardServerConfig) {
        Future<ResponseData> future = Future.future();

        JsonObject user = ctx.user().principal();

        SiteProps siteProps = SiteProps.builder()
            .microContext(microContext)
            .routingContext(ctx)
            .mongoClient(mongoClient)
            .dashboardServerConfig(dashboardServerConfig)
            .body(ctx.getBodyAsJson())
            .user(user)
            .companyId(getCompanyId(user))
            .role(getRole(user))
            .build();

        Single
            .just(getRole(user))
            .flatMap(this::userRolePermissionCheck)
            .flatMap(role -> postSite(siteProps))
            .subscribe(ignored -> future.complete(new ResponseData().setStatus(HttpResponseStatus.CREATED)),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Single<Role> userRolePermissionCheck(Role role) {
        if (role == Role.SUPER_ADMIN || role == Role.ADMIN) {
            return Single.just(role);
        } else {
            throw HttpException.forbidden();
        }
    }

    private Single<Boolean> postSite(SiteProps siteProps) {
        String associatedCompanyId = siteProps.getBody().getString("associated_company_id");
        return siteProps
            .getMongoClient()
            .rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
            .map(associatedCompany -> {
                if (associatedCompany != null) {
                    if (associatedCompany.getString("role").equals(Role.MANAGER.toString())) {
                        return associatedCompany;
                    } else {
                        throw HttpException.badRequest("You must associate Manager level company.");
                    }
                } else {
                    throw HttpException.badRequest("Failed to get the associated_company.");
                }
            })
            .flatMap(associatedCompany -> {
                if (siteProps.getRole() == Role.SUPER_ADMIN) {
                    return Single.just(associatedCompany);
                } else {
                    return byAdminCompanyGetManagerSelectionList(siteProps.getMongoClient(), siteProps.getCompanyId())
                        .map(companies -> {
                            if (companies.contains(associatedCompany.getString("_id"))) {
                                return associatedCompany;
                            } else {
                                throw HttpException.forbidden();
                            }
                        });
                }
            })
            .flatMap(associatedCompany -> siteProps
                .getMongoClient()
                .rxSave(SITE,
                        new Site(siteProps.getBody().put("associated_company_id", associatedCompany.getString("_id"))
                                     .put("role", Role.MANAGER.toString())).toJsonObject())
                .flatMap(siteId -> {
                    if (siteProps.getDashboardServerConfig().getDittoPolicy()) {
                        // We will create a fresh ditto policy for Site
                        siteProps.setSiteId(siteId);
                        return createDittoPolicySite(siteProps, associatedCompany).map(ignored -> true);
                    }
                    return Single.just(true);
                }));
    }

    // TODO: to make dispatchRequests working
    private Single<ResponseData> createDittoPolicySite(SiteProps siteProps, JsonObject associatedCompany) {
        return siteProps.getMongoClient()
            .rxFindOne(COMPANY, idQuery(associatedCompany.getString("_id")), null)
            .flatMap(managerLevelCompany -> siteProps.getMongoClient()
                .rxFindOne(COMPANY, idQuery(managerLevelCompany.getString("associated_company_id")), null)
                .flatMap(adminLevelCompany -> {
                    JsonObject findUser = new JsonObject()
                        .put("$or", new JsonArray().add(new JsonObject().put("role", Role.SUPER_ADMIN.toString()))
                            .add(new JsonObject().put("company_id", adminLevelCompany.getString("_id"))));

                    return siteProps.getMongoClient().rxFind(USER, findUser)
                        .flatMap(
                            users -> {
                                RequestData requestData = RequestDataConverter.convert(siteProps.getRoutingContext());
                                requestData.setBody(DittoUtils.createPolicy(users));
                                return dispatchRequests(siteProps.getMicroContext(), HttpMethod.PUT,
                                                        Services.POLICY_PREFIX + siteProps.getSiteId(),
                                                        requestData)
                                    .doOnError(throwable -> siteProps.getMongoClient()
                                        .removeDocuments(SITE, idQuery(siteProps.getSiteId()), null));
                            });
                }));
    }

}
