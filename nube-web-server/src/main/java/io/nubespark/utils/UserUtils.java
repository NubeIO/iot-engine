package io.nubespark.utils;

import io.nubespark.Role;
import io.nubespark.impl.models.KeycloakUserRepresentation;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;

import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

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

    public static void createUser(KeycloakUserRepresentation user, String accessToken, String authServerUrl,
                                  String realmName, HttpClient client, Handler<AsyncResult<JsonObject>> handler) {
        String uri = authServerUrl + "/admin/realms/" + realmName + "/users";

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response ->
                response.bodyHandler(body -> {
                    handler.handle(Future.succeededFuture(new JsonObject().put("statusCode", response.statusCode())));
                }));

        request.setChunked(true);
        request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        request.putHeader("Authorization", "Bearer " + accessToken);
        request.write(user.toJsonObject().toString()).end();
    }

    public static void getUser(String username, String accessToken, String authServerUrl,
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

        request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        request.putHeader("Authorization", "Bearer " + accessToken);
        request.end();
    }

    public static void resetPassword(String userId, String password, String accessToken, String authServerUrl,
                               String realmName, HttpClient client, Handler<AsyncResult<JsonObject>> handler) {
        String uri = authServerUrl + "/admin/realms/" + realmName + "/users/" + userId + "/reset-password";

        HttpClientRequest request = client.requestAbs(HttpMethod.PUT, uri, response ->
                response.bodyHandler(body -> {
                    handler.handle(Future.succeededFuture(new JsonObject()
                            .put("statusCode", response.statusCode())
                    ));
                })
        );

        request.setChunked(true);
        request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        request.putHeader("Authorization", "Bearer " + accessToken);
        JsonObject requestBody = new JsonObject()
                .put("temporary", false)
                .put("type", "password")
                .put("value", password);
        request.write(requestBody.toString()).end();
    }

    public static void deleteUser(String userId, String accessToken, String authServerUrl, String realmName,
                                  HttpClient client, Handler<AsyncResult<JsonObject>> handler) {
        String uri = authServerUrl + "/admin/realms/" + realmName + "/users/" + userId;

        HttpClientRequest request = client.requestAbs(HttpMethod.DELETE, uri, response-> {
           response.bodyHandler(body -> {
              handler.handle(Future.succeededFuture(new JsonObject()
                      .put("statusCode", response.statusCode())
              ));
           });
        });
        request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        request.putHeader("Authorization", "Bearer " + accessToken);
        request.end();
    }
}
