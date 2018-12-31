package com.nubeiot.dashboard.utils;

import static com.nubeiot.core.common.utils.response.ResponseUtils.CONTENT_TYPE;
import static com.nubeiot.core.common.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;

import com.nubeiot.core.common.utils.HttpException;
import com.nubeiot.core.common.utils.SQLUtils;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.impl.models.KeycloakUserRepresentation;

public class UserUtils {
    public static Role getReverseRole(Role userRole) {
        switch (userRole) {
            case GUEST:
                return Role.MANAGER;
            case USER:
                return Role.MANAGER;
            case MANAGER:
                return Role.ADMIN;
            case ADMIN:
                return Role.SUPER_ADMIN;
            default:
                return Role.SUPER_ADMIN;
        }
    }

    public static Role getRole(Role userRole) {
        switch (userRole) {
            case SUPER_ADMIN:
                return Role.ADMIN;
            case ADMIN:
                return Role.MANAGER;
            case MANAGER:
                return Role.USER;
            default:
                return Role.GUEST;
        }
    }

    public static Role getRole(Role companyRole, Role setRole) {
        switch (companyRole) {
            case SUPER_ADMIN:
                return setRole;
            case ADMIN:
                return SQLUtils.in(setRole.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString()) ? Role.MANAGER : setRole;
            case MANAGER:
                return (setRole == Role.USER) ? Role.USER : Role.GUEST;
            default:
                return Role.GUEST;
        }
    }

    public static Single<Buffer> createUser(KeycloakUserRepresentation user, String accessToken, String authServerUrl, String realmName, HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users";

        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.POST, url, response -> {
                response.bodyHandler(body -> {
                    if (response.statusCode() == 201) {
                        source.onSuccess(body);
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failed..."));
                    }
                });
            });

            request.setChunked(true);
            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.write(user.toJsonObject().toString()).end();
        });
    }

    public static Single<JsonObject> getUserFromUsername(String username, String accessToken, String authServerUrl, String realmName, HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users/?username=" + username;
        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.GET, url, response -> {
                response.bodyHandler(body -> {
                    System.out.println("Response users: " + body.toJsonArray());
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        source.onSuccess(new JsonObject(body.toJsonArray().getValue(0).toString()));
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failed..."));
                    }
                });
            });

            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.end();
        });
    }

    public static Single<JsonObject> getUser(String userId, String accessToken, String authServerUrl, String realmName, HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users/" + userId;
        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.GET, url, response -> {
                response.bodyHandler(body -> {
                    System.out.println("Response users: " + body.toJsonObject());
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        source.onSuccess(body.toJsonObject());
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failed..."));
                    }
                });
            });

            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.end();
        });
    }

    public static Single<Buffer> resetPassword(String userId, String password, String accessToken, String authServerUrl, String realmName, HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users/" + userId + "/reset-password";

        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.PUT, url, response -> {
                response.bodyHandler(body -> {
                    if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                        source.onSuccess(body);
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failure on resetting password."));
                    }
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
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        source.onSuccess(new JsonArray(body.getDelegate()));
                    } else {
                        source.onError(new HttpException(response.statusCode()));
                    }
                });
            });
            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.end();
        });
    }

    public static Single<Buffer> updateUser(String userId, KeycloakUserRepresentation keycloakUserRepresentation, String accessToken, String authServerUrl,
                                            String realmName, HttpClient client) {
        String url = authServerUrl + "/admin/realms/" + realmName + "/users/" + userId;

        return Single.create(source -> {
            HttpClientRequest request = client.requestAbs(HttpMethod.PUT, url, response -> {
                response.bodyHandler(body -> {
                    if (response.statusCode() != HttpResponseStatus.NO_CONTENT.code()) {
                        source.onError(new HttpException(response.statusCode()));
                    } else {
                        source.onSuccess(body);
                    }
                });
            });

            request.setChunked(true);
            request.putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
            request.putHeader("Authorization", "Bearer " + accessToken);
            request.write(keycloakUserRepresentation.toJsonObject().toString()).end();
        });
    }

    public static boolean isLastLevelUser(Role role) {
        return (role == Role.USER) || (role == Role.GUEST);
    }
}
