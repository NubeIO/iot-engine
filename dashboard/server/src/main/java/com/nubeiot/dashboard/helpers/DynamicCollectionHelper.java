package com.nubeiot.dashboard.helpers;

import java.util.UUID;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.mongo.MongoUtils;
import com.nubeiot.dashboard.DynamicCollectionProps;
import com.nubeiot.dashboard.Role;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class DynamicCollectionHelper {

    public static Future<ResponseData> handleGet(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);
        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId()) {

            mongoClient.rxFind(collection, new JsonObject().put("site_id", collectionProps.getSiteId()))
                       .subscribe(response -> {
                           if (response == null) {
                               future.complete(new ResponseData().setStatusCode(HttpResponseStatus.NOT_FOUND.code()));
                           } else {
                               future.complete(new ResponseData().setBodyMessage(response.toString()));
                           }
                       }, throwable -> future.complete(ResponseDataHelper.internalServerError(throwable.getMessage())));
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    public static Future<ResponseData> handleGetOne(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        String id = ctx.request().getParam("id");
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);

        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId()) {
            JsonObject query = new JsonObject().put("site_id", collectionProps.getSiteId()).put("id", id);
            mongoClient.rxFindOne(collection, query, null).subscribe(response -> {
                if (response == null) {
                    future.complete(new ResponseData().setStatusCode(HttpResponseStatus.NOT_FOUND.code()));
                } else {
                    future.complete(new ResponseData().setBodyMessage(response.encode()));
                }
            }, throwable -> future.complete(ResponseDataHelper.internalServerError(throwable.getMessage())));
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    public static Future<ResponseData> handlePost(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);
        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId() && collectionProps.getRole() != Role.GUEST) {
            handlePostDocument(ctx, mongoClient, collection, collectionProps.getSiteId(), future);
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    public static Future<ResponseData> handlePut(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);

        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId() && collectionProps.getRole() != Role.GUEST) {
            handlePutDocument(ctx, mongoClient, collection, collectionProps.getSiteId(), future);
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    public static Future<ResponseData> handleDelete(RoutingContext ctx, MongoClient mongoClient, String collection) {
        Future<ResponseData> future = Future.future();
        DynamicCollectionProps collectionProps = new DynamicCollectionProps(ctx, collection);
        if (collectionProps.getSitesIds().size() == 0) {
            future.complete(ResponseDataHelper.badRequest("User must be associated with <SiteSetting>"));
        } else if (collectionProps.isAuthorizedSiteId() && collectionProps.getRole() != Role.GUEST) {
            handleDeleteDocument(ctx, mongoClient, collection, collectionProps.getSiteId(), future);
        } else {
            future.complete(ResponseDataHelper.forbidden());
        }
        return future;
    }

    public static void handleDeleteDocument(RoutingContext ctx, MongoClient mongoClient, String collection,
                                            String siteId, Future<ResponseData> future) {
        String id = ctx.request().getParam("id");
        JsonObject query = new JsonObject().put("site_id", siteId).put("id", id);
        mongoClient.rxRemoveDocuments(collection, query).subscribe(buffer -> {
            future.complete(new ResponseData().setStatusCode(HttpResponseStatus.NO_CONTENT.code()));
        }, throwable -> future.complete(ResponseDataHelper.internalServerError(throwable.getMessage())));
    }

    private static void handlePostDocument(RoutingContext ctx, MongoClient mongoClient, String collection,
                                           String siteId, Future<ResponseData> future) {
        String id = UUID.randomUUID().toString();
        mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId).put("id", id)).map(response -> {
            JsonObject body = ctx.getBodyAsJson();
            if (response.size() > 0) {
                future.complete(new ResponseData().setStatusCode(HttpResponseStatus.CONFLICT.code())
                                                  .setBodyMessage("We have already that id value."));
            }
            body.put("site_id", siteId);
            body.put("id", id);
            return body;
        }).flatMap(body -> MongoUtils.postDocument(mongoClient, collection, body)).subscribe(buffer -> {
            future.complete(new ResponseData().setStatusCode(HttpResponseStatus.CREATED.code()));
        }, throwable -> {
            future.complete(ResponseDataHelper.internalServerError(throwable.getMessage()));
        });
    }

    public static void handlePutDocument(RoutingContext ctx, MongoClient mongoClient, String collection, String siteId,
                                         Future<ResponseData> future) {
        String id = ctx.request().getParam("id");
        mongoClient.rxFindOne(collection, new JsonObject().put("site_id", siteId).put("id", id), null)
                   .map(jsonObject -> {
                       JsonObject body = ctx.getBodyAsJson();
                       if (jsonObject != null) {
                           body.put("_id", jsonObject.getString("_id"));
                       }
                       body.put("site_id", siteId);
                       body.put("id", id);
                       return body;
                   })
                   .flatMap(body -> mongoClient.rxSave(collection, body))
                   .subscribe(buffer -> {
                       future.complete(new ResponseData());
                   }, throwable -> future.complete(ResponseDataHelper.internalServerError(throwable.getMessage())));
    }

}
