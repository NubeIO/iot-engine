package com.nubeiot.dashboard.controllers;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.dashboard.handlers.DynamicCollectionHandler;
import com.zandero.rest.annotation.RouteOrder;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

@Path("/api/layout_grid")
public class LayoutGridController implements RestApi {

    private static String COLLECTION = "layout_grid";

    @GET
    @Path("/")
    @RouteOrder(3)
    public Future<ResponseData> get(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return DynamicCollectionHandler.handleGet(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @GET
    @Path("/:id")
    @RouteOrder(3)
    public Future<ResponseData> getOne(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return DynamicCollectionHandler.handleGetOne(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @POST
    @Path("/")
    @RouteOrder(3)
    public Future<ResponseData> post(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return DynamicCollectionHandler.handlePost(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @PUT
    @Path("/:id")
    @RouteOrder(3)
    public Future<ResponseData> put(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return DynamicCollectionHandler.handlePut(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

    @DELETE
    @Path("/:id")
    @RouteOrder(3)
    public Future<ResponseData> delete(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient) {
        return DynamicCollectionHandler.handleDelete(ctx, mongoClient.getMongoClient(), COLLECTION);
    }

}
