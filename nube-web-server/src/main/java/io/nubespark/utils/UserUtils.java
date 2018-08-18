package io.nubespark.utils;

import io.nubespark.Role;
import io.nubespark.KeycloakUserRepresentation;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;

public class UserUtils {
    public static Role getRole(Role userRole, Role setRole) {
        switch (userRole) {
            case SUPER_ADMIN:
                return Role.ADMIN;
            case ADMIN:
                return Role.MANAGER;
            case MANAGER:
                return (setRole == Role.USER) ? Role.USER : Role.GUEST;
            default:
                return Role.GUEST;
        }
    }

    public static void createUser(KeycloakUserRepresentation user, String access_token, String authServerUrl,
                                  String realmName, HttpClient client, Handler<AsyncResult<JsonObject>> handler) {
        String uri = authServerUrl + "/admin/realms/" + realmName + "/users";

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response ->
                response.bodyHandler(body -> {
                    System.out.println(response.statusCode() + "is the output status code");
                    System.out.println(body + "is the output body");
                    handler.handle(Future.succeededFuture(new JsonObject().put("statusCode", response.statusCode())));
                }));

        System.out.println("AccessToken:::" + access_token);

        request.setChunked(true);
        request.putHeader("content-type", "application/json");
        request.putHeader("Authorization", "Bearer " + access_token);

        request.write(user.toJson().toString()).end();
    }

    public static void getUser(String username, String access_token, String authServerUrl,
                               String realmName, HttpClient client, Handler<AsyncResult<JsonObject>> handler) {
        String uri = authServerUrl + "/admin/realms/" + realmName + "/users/?username=" + username;

        HttpClientRequest request = client.requestAbs(HttpMethod.GET, uri, response ->
                response.bodyHandler(body -> {
                    handler.handle(Future.succeededFuture(new JsonObject()
                            .put("body", body.toJsonArray().getValue(0))
                            .put("statusCode", response.statusCode())
                    ));
                })
        );

        request.putHeader("content-type", "application/json");
        request.putHeader("Authorization", "Bearer " + access_token);
        request.end();
    }
}
