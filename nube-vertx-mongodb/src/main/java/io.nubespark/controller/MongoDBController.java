package io.nubespark.controller;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

public class MongoDBController {
    private MongoClient client;

    public MongoDBController(MongoClient client) {
        this.client = client;
    }

    public void getAll(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String document = request.getParam("document");
        // we can use this query for filtering output array
        JsonObject query = new JsonObject();
        client.find(document, query, res -> {
            if (res.succeeded()) {
                routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .end(Json.encodePrettily(res.result()));
            } else {
                res.cause().printStackTrace();
                routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
            }
        });
    }

    public void getOne(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String document = request.getParam("document");
        String id = request.getParam("id");
        JsonObject searchQuery = new JsonObject().put("_id", id);
        client.findOne(document, searchQuery, null, res -> {
            if (res.succeeded()) {
                routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .end(Json.encodePrettily(res.result()));
            } else {
                res.cause().printStackTrace();
                routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            }
        });
    }

    public void save(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String document = request.getParam("document");
        JsonObject data = routingContext.getBodyAsJson();

        try {
            // Converting integer id into string;
            // Since we don't need the conflicting behaviour of getting values for two different string and integer ids
            if (data.getInteger("_id").toString().matches("\\d+")) {
                data.put("_id", data.getInteger("_id").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.save(document, data, res -> {
            if (res.succeeded()) {
                routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .end(Json.encodePrettily(new JsonObject().put("result", res.result())));
            } else {
                res.cause().printStackTrace();
                routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            }
        });
    }

    public void deleteAll(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String document = request.getParam("document");
        client.dropCollection(document, res -> {
            if (res.succeeded()) {
                routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
                        .end(Json.encodePrettily(res.result()));
            } else {
                res.cause().printStackTrace();
                routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            }
        });
    }

    public void deleteOne(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String document = request.getParam("document");
        String id = request.getParam("id");
        JsonObject searchQuery = new JsonObject().put("_id", id);
        client.findOneAndDelete(document, searchQuery, res -> {
            if (res.succeeded()) {
                routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
                        .end(Json.encodePrettily(res.result()));
            } else {
                res.cause().printStackTrace();
                routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            }
        });
    }
}
