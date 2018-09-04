package io.nubespark.controller;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
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
        if (routingContext.request().method() == HttpMethod.POST) {
            query = routingContext.getBodyAsJson();
        }
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

    public void put(RoutingContext routingContext) {
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
        saveData(document, data, routingContext);
    }

    public void post(RoutingContext routingContext) {
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
        if (data.getString("_id") != null) {
            client.findOne(document, new JsonObject().put("_id", data.getString("_id")), null, res -> {
                if (res.result() != null) {
                    routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(HttpResponseStatus.CONFLICT.code())
                        .end(Json.encodePrettily(new JsonObject().put("result", res.result()).put("statusCode", HttpResponseStatus.CONFLICT.code())));
                } else {
                    saveData(document, data, routingContext);
                }
            });
        } else {
            saveData(document, data, routingContext);
        }

    }

    public void deleteAll(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String document = request.getParam("document");

        // we can use this query for filtering output array
        if (routingContext.request().method() == HttpMethod.POST) {
            JsonObject query = routingContext.getBodyAsJson();
            client.removeDocuments(document, query, res -> {
                if (res.succeeded()) {
                    routingContext.response()
                        .setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                } else {
                    res.cause().printStackTrace();
                    routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encodePrettily(new JsonObject().put("result", res.result()).put("statusCode", HttpResponseStatus.BAD_REQUEST.code())));
                }
            });
        } else {
            client.dropCollection(document, res -> {
                if (res.succeeded()) {
                    routingContext.response()
                        .setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
                } else {
                    res.cause().printStackTrace();
                    routingContext.response()
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                        .end(Json.encodePrettily(new JsonObject().put("result", res.result()).put("statusCode", HttpResponseStatus.BAD_REQUEST.code())));
                }
            });
        }
    }

    public void deleteOne(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String document = request.getParam("document");
        String id = request.getParam("id");
        JsonObject searchQuery = new JsonObject().put("_id", id);
        client.findOneAndDelete(document, searchQuery, res -> {
            if (res.succeeded()) {
                System.out.println("Delete one is being called...");
                routingContext.response()
                    .setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
            } else {
                res.cause().printStackTrace();
                routingContext.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end(Json.encodePrettily(new JsonObject().put("result", res.result()).put("statusCode", HttpResponseStatus.BAD_REQUEST.code())));
            }
        });
    }

    private void saveData(String document, JsonObject data, RoutingContext routingContext) {
        client.save(document, data, res -> {
            if (res.succeeded()) {
                routingContext.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(HttpResponseStatus.CREATED.code())
                    .end(Json.encodePrettily(new JsonObject().put("result", res.result()).put("statusCode", HttpResponseStatus.CREATED.code())));
            } else {
                res.cause().printStackTrace();
                routingContext.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end(Json.encodePrettily(new JsonObject().put("result", res.result()).put("statusCode", HttpResponseStatus.BAD_REQUEST.code())));
            }
        });
    }
}
