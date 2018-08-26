package io.nubespark.utils;

import io.nubespark.Role;
import io.nubespark.impl.models.KeycloakUserRepresentation;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;

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

    public static Single<JsonObject> createUser(KeycloakUserRepresentation user, String accessToken, String authServerUrl, String realmName, HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users";

        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.POST, url, response -> {
                response.bodyHandler(body -> {
                    source.onSuccess(new JsonObject().put("statusCode", response.statusCode()));
                });
            });

            request.setChunked(true);
            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.write(user.toJsonObject().toString()).end();
        });
    }

    public static Single<JsonObject> getUser(String username, String accessToken, String authServerUrl, String realmName, HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users/?username=" + username;

        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.GET, url, response -> {
                response.bodyHandler(body -> {
                    source.onSuccess(new JsonObject()
                            .put("body", body.toJsonArray().getValue(0))
                            .put("statusCode", response.statusCode()));
                });
            });

            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.end();
        });
    }

    public static Single<JsonObject> resetPassword(String userId, String password, String accessToken, String authServerUrl, String realmName, HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users/" + userId + "/reset-password";

        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.PUT, url, response -> {
                response.bodyHandler(body -> {
                    source.onSuccess(new JsonObject().put("statusCode", response.statusCode()));
                });
            });

            request.setChunked(true);
            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            JsonObject requestBody = new JsonObject()
                    .put("temporary", false)
                    .put("type", "password")
                    .put("value", password);
            request.write(requestBody.toString()).end();
        });
    }

    public static Single<JsonObject> deleteUser(String userId, String accessToken, String authServerUrl, String realmName,
                                                HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users/" + userId;

        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.DELETE, url, response -> {
                response.bodyHandler(body -> {
                    source.onSuccess(new JsonObject().put("statusCode", response.statusCode()));
                });
            });
            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.end();
        });
    }

    public static Single<JsonArray> queryUsers(String query, String accessToken, String authServerUrl, String realmName,
                                                HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users?" + query;

        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.GET, url, response -> {
                response.bodyHandler(body -> {
                    source.onSuccess(new JsonArray(body.getDelegate()));
                });
            });
            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.end();
        });
    }
}
