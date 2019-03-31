package com.nubeiot.dashboard.controllers;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.helpers.DynamicCollectionHelper;
import com.nubeiot.dashboard.helpers.ResponseDataHelper;
import com.nubeiot.dashboard.props.DynamicCollectionProps;
import com.zandero.rest.annotation.RouteOrder;

@Path("/api/menu")
public class MenuController implements RestApi {

    private static final String COLLECTION = "menu";

    @GET
    @Path("/:id")
    @RouteOrder(3)
    public Future<ResponseData> getOne(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return DynamicCollectionHelper.handleGetOne(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @PUT
    @Path("/:id")
    @RouteOrder(3)
    public Future<ResponseData> put(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return this.handlePut(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @PUT
    @Path("/:id/:menu_id")
    @RouteOrder(3)
    public Future<ResponseData> putRecord(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return this.handlePutRecord(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @DELETE
    @Path("/:id")
    @RouteOrder(3)
    public Future<ResponseData> delete(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return this.handleDelete(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @DELETE
    @Path("/:id/:menu_id")
    @RouteOrder(3)
    public Future<ResponseData> deleteRecord(@Context RoutingContext ctx,
                                             @Context RestMongoClientProvider mongoClient) {
        return this.handleDeleteRecord(ctx, mongoClient.getMongoClient(), COLLECTION);
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
        mongoClient.rxUpdate(collection, query, update).subscribe(() -> {
            future.complete(new ResponseData());
        }, throwable -> {
            future.complete(ResponseDataHelper.internalServerError(throwable.getMessage()));
        });
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
        mongoClient.rxUpdate(collection, query, update).subscribe(() -> {
            future.complete(new ResponseData());
        }, throwable -> {
            future.complete(ResponseDataHelper.internalServerError(throwable.getMessage()));
        });
    }

}
