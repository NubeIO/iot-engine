package com.nubeiot.dashboard.controllers;

import static com.nubeiot.core.http.handler.ResponseDataWriter.responseData;
import static com.nubeiot.dashboard.Role.ADMIN;
import static com.nubeiot.dashboard.constants.Collection.MENU;
import static com.nubeiot.dashboard.utils.UserUtils.getRole;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.helpers.DynamicCollectionHelper;
import com.nubeiot.dashboard.helpers.ResponseDataHelper;
import com.nubeiot.dashboard.props.DynamicCollectionProps;
import com.zandero.rest.annotation.RouteOrder;

public class MenuController implements RestApi {

    private static final String COLLECTION = "menu";

    @GET
    @Path("/menu/:id")
    @RouteOrder(3)
    public Future<ResponseData> getOne(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return DynamicCollectionHelper.handleGetOne(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @PUT
    @Path("/menu/:id")
    @RouteOrder(3)
    public Future<ResponseData> put(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return this.handlePut(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @PUT
    @Path("/menu/:id/:menu_id")
    @RouteOrder(3)
    public Future<ResponseData> putRecord(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return this.handlePutRecord(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @DELETE
    @Path("/menu/:id")
    @RouteOrder(3)
    public Future<ResponseData> delete(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return this.handleDelete(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @DELETE
    @Path("/menu/:id/:menu_id")
    @RouteOrder(3)
    public Future<ResponseData> deleteRecord(@Context RoutingContext ctx,
                                             @Context RestMongoClientProvider mongoClient) {
        return this.handleDeleteRecord(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @GET
    @Path("/menu_for_user_group/:id")
    @RouteOrder(3)
    public Future<ResponseData> menuForUserGroup(@Context RoutingContext ctx,
                                                 @Context RestMongoClientProvider mongoClient) {
        return this.handleMenuForUserGroup(ctx, mongoClient.getMongoClient());
    }

    private Future<ResponseData> handleMenuForUserGroup(RoutingContext ctx, MongoClient mongoClient) {
        Future<ResponseData> future = Future.future();
        JsonObject user = ctx.user().principal();

        Single.just(getRole(user))
            .flatMap(role -> {
                if (role == Role.SUPER_ADMIN || role == ADMIN) {
                    String siteId = ctx.request().getParam("id");
                    return mongoClient.rxFindOne(MENU, new JsonObject().put("site_id", siteId), null)
                        .map(menu -> menu != null ? menu : new JsonObject());
                } else {
                    throw HttpException.forbidden();
                }
            })
            .subscribe(menu -> future.complete(responseData(menu.toString())),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
        return future;
    }

    private Future<ResponseData> handlePut(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);
        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId() && collectionProps.getRole() != Role.GUEST) {
            DynamicCollectionHelper.handlePutDocument(ctx, mongoClient, collection, collectionProps.getSiteId(),
                                                      future);
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    private Future<ResponseData> handlePutRecord(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);
        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId() && collectionProps.getRole() != Role.GUEST) {
            this.handleMenuPutRecord(ctx, mongoClient, collection, collectionProps.getSiteId(), future);
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    private void handleMenuPutRecord(RoutingContext ctx, MongoClient mongoClient, String collection, String siteId,
                                     Future<ResponseData> future) {
        JsonObject body = ctx.getBodyAsJson();
        String id = ctx.request().getParam("id");
        String menuId = ctx.request().getParam("menu_id");
        body.put("id", menuId);
        JsonObject query = new JsonObject().put("site_id", siteId).put("id", id).put("menu.id", menuId);
        JsonObject update = new JsonObject().put("$set", new JsonObject().put("menu.$", body));
        mongoClient.rxUpdateCollection(collection, query, update)
            .subscribe(ignored -> future.complete(new ResponseData()),
                       throwable -> future.complete(ResponseDataHelper.internalServerError(throwable.getMessage())));
    }

    private Future<ResponseData> handleDelete(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);
        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId() && collectionProps.getRole() != Role.GUEST) {
            DynamicCollectionHelper.handleDeleteDocument(ctx, mongoClient, collection, collectionProps.getSiteId(),
                                                         future);
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    private Future<ResponseData> handleDeleteRecord(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);
        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId() && collectionProps.getRole() != Role.GUEST) {
            handleMenuDeleteRecord(ctx, mongoClient, collection, collectionProps.getSiteId(), future);
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    private void handleMenuDeleteRecord(RoutingContext ctx, MongoClient mongoClient, String collection, String siteId,
                                        Future<ResponseData> future) {
        String id = ctx.request().getParam("id");
        String menuId = ctx.request().getParam("menu_id");
        JsonObject query = new JsonObject().put("site_id", siteId).put("id", id).put("menu.id", menuId);
        JsonObject update = new JsonObject().put("$pull",
                                                 new JsonObject().put("menu", new JsonObject().put("id", menuId)));
        mongoClient.rxUpdateCollection(collection, query, update)
            .subscribe(ignored -> future.complete(new ResponseData()),
                       throwable -> future.complete(ResponseDataHelper.internalServerError(throwable.getMessage())));
    }

}
