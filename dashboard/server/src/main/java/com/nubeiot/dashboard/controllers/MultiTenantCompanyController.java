package com.nubeiot.dashboard.controllers;

import static com.nubeiot.core.mongo.MongoUtils.idQuery;
import static com.nubeiot.dashboard.constants.Collection.COMPANY;
import static com.nubeiot.dashboard.helpers.MultiTenantPermissionHelper.checkPermissionAndReturnValue;
import static com.nubeiot.dashboard.helpers.MultiTenantRepresentationHelper.associatedCompanyRepresentation;
import static com.nubeiot.dashboard.utils.UserUtils.getCompanyId;
import static com.nubeiot.dashboard.utils.UserUtils.getRole;

import java.util.List;
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
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.core.utils.SQLUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.models.Company;
import com.nubeiot.dashboard.utils.UserUtils;
import com.zandero.rest.annotation.RouteOrder;

@Path("/api")
public class MultiTenantCompanyController implements RestApi {

    @GET
    @Path("/companies")
    @RouteOrder(3)
    public Future<ResponseData> get(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleGetCompanies(ctx, mongoClient.getMongoClient());
    }

    @POST
    @Path("/company")
    @RouteOrder(3)
    public Future<ResponseData> post(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handlePostCompany(ctx, mongoClient.getMongoClient());
    }

    @POST
    @Path("/delete_companies")
    @RouteOrder(3)
    public Future<ResponseData> delete(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleDeleteCompanies(ctx, mongoClient.getMongoClient());
    }

    @PATCH
    @Path("/company/:id")
    @RouteOrder(3)
    public Future<ResponseData> patch(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return handleUpdateCompany(ctx, mongoClient.getMongoClient());
    }

    private Future<ResponseData> handleUpdateCompany(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject body = ctx.getBodyAsJson();
        JsonObject user = ctx.user().principal();
        String companyId = ctx.request().getParam("id");

        Single.just(getRole(user))
            .flatMap(role -> createUpdateCompanyModel(mongoClient, body, user, companyId, role))
            .flatMap(company -> mongoClient.rxSave(COMPANY, company.toJsonObject().put("_id", companyId)))
            .subscribe(
                ignore -> future.complete(new ResponseData().setStatusCode(HttpResponseStatus.NO_CONTENT.code())),
                throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Single<Company> createUpdateCompanyModel(MongoClient mongoClient, JsonObject body, JsonObject user,
                                                     String companyId, Role role) {
        if (role == Role.SUPER_ADMIN) {
            return updateCompanyBySuperAdmin(mongoClient, body, companyId);
        } else if (role == Role.ADMIN) {
            return updateCompanyByAdmin(mongoClient, body, user, companyId);
        } else {
            throw HttpException.forbidden();
        }
    }

    private Future<ResponseData> handleDeleteCompanies(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        String companyId = getCompanyId(user);

        Single
            .just(getRole(user))
            .flatMap(role -> deleteCompanies(ctx, mongoClient, companyId, role))
            .subscribe(
                ignored -> future.complete(new ResponseData().setStatusCode(HttpResponseStatus.NO_CONTENT.code())),
                throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Single<MongoClientDeleteResult> deleteCompanies(RoutingContext ctx, MongoClient mongoClient,
                                                            String companyId, Role role) {
        // Model level permission; this is limited to SUPER_ADMIN and ADMIN
        if (SQLUtils.in(role.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())) {
            JsonArray queryInput = ctx.getBodyAsJsonArray();
            // Object level permission
            JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", queryInput));
            return mongoClient.rxFind(COMPANY, query)
                .flatMap(companies -> {
                    if (companies.size() == queryInput.size()) {
                        return checkPermissionAndReturnValue(mongoClient, companyId, role, companies);
                    } else {
                        throw HttpException.badRequest("Doesn't have those <Companies> on Database.");
                    }
                })
                .flatMap(ignored -> mongoClient.rxRemoveDocuments(COMPANY, query));
        } else {
            throw HttpException.forbidden();
        }
    }

    private Future<ResponseData> handleGetCompanies(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        String companyId = getCompanyId(user);

        Single
            .just(getRole(user))
            .flatMap(role -> getCompanies(mongoClient, companyId, role))
            .flatMap(response -> Observable.fromIterable(response)
                .flatMapSingle(company -> associatedCompanyRepresentation(mongoClient, company)).toList())
            .subscribe(response -> future.complete(new ResponseData().setBodyMessage(response.toString())),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Single<List<JsonObject>> getCompanies(MongoClient mongoClient, String companyId, Role role) {
        if (role == Role.SUPER_ADMIN) {
            return mongoClient.rxFind(COMPANY, new JsonObject()
                .put("role", new JsonObject().put("$not", new JsonObject().put("$eq", Role.SUPER_ADMIN.toString()))));
        } else if (role == Role.ADMIN) {
            return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId));
        } else {
            throw HttpException.forbidden();
        }
    }

    private Future<ResponseData> handlePostCompany(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();
        JsonObject body = ctx.getBodyAsJson();

        Single
            .just(getRole(user))
            .flatMap(role -> createPostCompanyModel(mongoClient, user, body, role))
            .flatMap(company -> mongoClient.rxSave(COMPANY, company.toJsonObject()))
            .subscribe(ignored -> future.complete(new ResponseData()),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));

        return future;
    }

    private Single<Company> createPostCompanyModel(MongoClient mongoClient, JsonObject user, JsonObject body,
                                                   Role role) {
        if (role == Role.SUPER_ADMIN) {
            return postCompanyBySuperAdmin(mongoClient, user, body);
        } else if (role == Role.ADMIN) {
            return postCompanyByAdmin(user, body);
        } else {
            throw HttpException.forbidden();
        }
    }

    private Single<Company> updateCompanyByAdmin(MongoClient mongoClient, JsonObject body, JsonObject user,
                                                 String companyId) {
        return mongoClient.rxFindOne(COMPANY, idQuery(companyId), null).map(company -> {
            if (company != null && company.getString("role").equals(Role.MANAGER.toString()) &&
                company.getString("associated_company_id").equals(getCompanyId(user))) {
                return new Company(body.put("role", company.getString("role"))
                                       .put("associated_company_id",
                                            company.getString("associated_company_id")));
            } else {
                throw HttpException.forbidden();
            }
        });
    }

    private Single<Company> updateCompanyBySuperAdmin(MongoClient mongoClient, JsonObject body, String companyId) {
        return mongoClient.rxFindOne(COMPANY, idQuery(companyId), null).flatMap(company -> {
            if (company != null) {
                if (company.getString("role").equals(Role.ADMIN.toString())) {
                    // role and associated_company_id will remain same
                    return Single.just(new Company(body.put("role", Role.ADMIN.toString())
                                                       .put("associated_company_id",
                                                            company.getString("associated_company_id"))));
                } else if (company.getString("role").equals(Role.MANAGER.toString())) {
                    return mongoClient.rxFindOne(COMPANY, idQuery(body.getString("associated_company_id", "")), null)
                        .map(associatedCompany -> {
                            if (associatedCompany != null &&
                                UserUtils.getRole(Role.valueOf(associatedCompany.getString("role"))) == Role.MANAGER) {
                                // Can change associated_company_id on the same level
                                return new Company(body.put("role", Role.MANAGER.toString()));
                            } else {
                                throw HttpException.badRequest("You can't associated that <Company>!");
                            }
                        });
                } else {
                    throw HttpException.badRequest("You can't change this company.");
                }
            } else {
                throw HttpException.badRequest("Requested <Company> doesn't exist!");
            }
        });
    }

    private Single<Company> postCompanyByAdmin(JsonObject user, JsonObject body) {
        Company company = new Company(body.put("associated_company_id", getCompanyId(user))
                                          .put("role", Role.MANAGER.toString()));
        return Single.just(company);
    }

    private Single<Company> postCompanyBySuperAdmin(MongoClient mongoClient, JsonObject user, JsonObject body) {
        String associatedCompanyId = body.getString("associated_company_id", "");
        if (Strings.isNotBlank(associatedCompanyId)) {
            return mongoClient.rxFindOne(COMPANY, idQuery(associatedCompanyId), null)
                .map(company -> {
                    if (company != null) {
                        Role role = UserUtils.getRole(Role.valueOf(company.getString("role")));
                        if (!SQLUtils.in(role.toString(), Role.ADMIN.toString(), Role.MANAGER.toString())) {
                            throw HttpException.badRequest("You can't associated that <Company>!");
                        }
                        return new Company(body.put("role", role));
                    } else {
                        throw HttpException.badRequest("Requested <Company> doesn't exist!");
                    }
                });
        } else {
            Company company = new Company(body.put("associated_company_id", getCompanyId(user))
                                              .put("role", Role.ADMIN.toString()));
            return Single.just(company);
        }
    }

}
