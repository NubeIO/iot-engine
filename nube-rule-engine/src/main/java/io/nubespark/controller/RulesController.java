package io.nubespark.controller;

import com.hazelcast.nio.ConnectionType;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.Collections;

import static io.nubespark.utils.response.ResponseUtils.*;

/**
 * Created by topsykretts on 4/26/18.
 */
public class RulesController {

    Vertx vertx;
    ServiceDiscovery discovery;

    public RulesController(Vertx vertx) {
        this.vertx = vertx;
    }

    public RulesController(Vertx vertx, ServiceDiscovery discovery) {
        this.vertx = vertx;
        this.discovery = discovery;
    }

    public void createRule(RoutingContext routingContext) {

    }

    public void getAll(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        System.out.println(request.toString());
        routingContext.response()
                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(new JsonObject()
                        .put("controller", "rules")
                        .put("action", "getAll")
                        .put("desc", "Get All Rules in system")
                ));
    }

    public void getOne(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String id = request.getParam("id");
        // just a test
        String query = "SELECT * FROM metadata where " + id + " = ? ";
        JsonObject queryObj = new JsonObject();
        queryObj.put("query", query);
        queryObj.put("params", new JsonArray(Collections.singletonList("m:")));
        vertx.eventBus().send("io.nubespark.jdbc.engine", queryObj, message -> {
            JsonObject replyJson = new JsonObject()
                    .put("controller", "rules")
                    .put("action", "getOne")
                    .put("desc", "Get singe rule for given param id")
                    .put("id", id);
            if (message.succeeded()) {
                Object reply = message.result().body();
                if (reply != null) {
                    replyJson.put("reply", reply);
                }
            } else {
                message.cause().printStackTrace();
                System.out.println(message.cause().getLocalizedMessage());
                System.out.println("Failed to receive reply...");
            }
            routingContext.response()
                    .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .end(Json.encodePrettily(replyJson));
        });

    }
}
