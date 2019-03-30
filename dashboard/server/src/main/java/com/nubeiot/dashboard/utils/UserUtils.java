package com.nubeiot.dashboard.utils;

import static com.nubeiot.core.http.ApiConstants.CONTENT_TYPE;
import static com.nubeiot.core.http.ApiConstants.DEFAULT_CONTENT_TYPE;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.utils.SQLUtils;
import com.nubeiot.dashboard.MultiTenantUserProps;
import com.nubeiot.dashboard.Role;

public class UserUtils {

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
                return SQLUtils.in(setRole.toString(), Role.SUPER_ADMIN.toString(), Role.ADMIN.toString())
                       ? Role.MANAGER
                       : setRole;
            case MANAGER:
                return (setRole == Role.USER) ? Role.USER : Role.GUEST;
            default:
                return Role.GUEST;
        }
    }

    public static Single<Buffer> createUser(MultiTenantUserProps userProps) {
        String url = userProps.getAuthServerUrl() + "/admin/realms/" + userProps.getRealmName() + "/users";
        return Single.create(source -> {
            HttpClientRequest request = userProps.getHttpClient().requestAbs(HttpMethod.POST, url, response ->
                response.bodyHandler(body -> {
                    if (response.statusCode() == 201) {
                        source.onSuccess(body);
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failed to create User."));
                    }
                }));
            request.setChunked(true);
            request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            request.putHeader("Authorization", "Bearer " + userProps.getAccessToken());
            request.write(userProps.getKeycloakUser().encode()).end();
        });
    }

    public static Single<JsonObject> getUserFromUsername(MultiTenantUserProps userProps) {
        String url =
            userProps.getAuthServerUrl() + "/admin/realms/" + userProps.getRealmName() + "/users/?username=" +
            userProps.getBodyUsername();
        return Single.create(source -> {
            HttpClientRequest request = userProps.getHttpClient().requestAbs(HttpMethod.GET, url, response ->
                response.bodyHandler(body -> {
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        source.onSuccess(new JsonObject(body.toJsonArray().getValue(0).toString()));
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failure to get User from Username."));
                    }
                }));
            request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            request.putHeader("Authorization", "Bearer " + userProps.getAccessToken());
            request.end();
        });
    }

    public static Single<JsonObject> getUser(MultiTenantUserProps userProps) {
        String url = userProps.getAuthServerUrl() + "/admin/realms/" + userProps.getRealmName() + "/users/" +
                     userProps.getParamsUserId();
        return Single.create(source -> {
            HttpClientRequest request = userProps.getHttpClient().requestAbs(HttpMethod.GET, url, response ->
                response.bodyHandler(body -> {
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        source.onSuccess(body.toJsonObject());
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failure on getting User."));
                    }
                }));
            request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            request.putHeader("Authorization", "Bearer " + userProps.getAccessToken());
            request.end();
        });
    }

    public static Single<Buffer> resetPassword(MultiTenantUserProps userProps) {
        String url = userProps.getAuthServerUrl() + "/admin/realms/" + userProps.getRealmName() + "/users/" +
                     userProps.getUserId() + "/reset-password";
        return Single.create(source -> {
            HttpClientRequest request = userProps.getHttpClient().requestAbs(HttpMethod.PUT, url, response ->
                response.bodyHandler(body -> {
                    if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                        source.onSuccess(body);
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failure on resetting Password."));
                    }
                }));
            request.setChunked(true);
            request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            request.putHeader("Authorization", "Bearer " + userProps.getAccessToken());
            JsonObject requestBody = new JsonObject()
                .put("temporary", false)
                .put("type", "password")
                .put("value", userProps.getBodyPassword());
            request.write(requestBody.encode()).end();
        });
    }

    public static Single<JsonObject> deleteUser(MultiTenantUserProps userProps) {
        String url =
            userProps.getAuthServerUrl() + "/admin/realms/" + userProps.getRealmName() + "/users/" +
            userProps.getParamsUserId();
        return Single.create(source -> {
            HttpClientRequest request = userProps.getHttpClient().requestAbs(HttpMethod.DELETE, url, response ->
                response
                    .bodyHandler(body -> source.onSuccess(new JsonObject().put("statusCode", response.statusCode()))));
            request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            request.putHeader("Authorization", "Bearer " + userProps.getAccessToken());
            request.end();
        });
    }

    public static Single<JsonArray> queryUsers(MultiTenantUserProps userProps, String query) {
        String url = userProps.getAuthServerUrl() + "/admin/realms/" + userProps.getRealmName() + "/users?" + query;
        return Single.create(source -> {
            HttpClientRequest request = userProps.getHttpClient().requestAbs(HttpMethod.GET, url, response ->
                response.bodyHandler(body -> {
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        source.onSuccess(new JsonArray(body));
                    } else {
                        source.onError(new HttpException(response.statusCode(), "Failure on querying Users."));
                    }
                }));
            request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            request.putHeader("Authorization", "Bearer " + userProps.getAccessToken());
            request.end();
        });
    }

    public static Single<Buffer> updateUser(MultiTenantUserProps userProps) {
        String url =
            userProps.getAuthServerUrl() + "/admin/realms/" + userProps.getRealmName() + "/users/" +
            userProps.getParamsUserId();
        return Single.create(source -> {
            HttpClientRequest request = userProps.getHttpClient()
                .requestAbs(HttpMethod.PUT, url, response ->
                    response.bodyHandler(body -> {
                        if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                            source.onSuccess(body);
                        } else {
                            source.onError(new HttpException(response.statusCode(), "Failure on Updating User."));
                        }
                    }));
            request.setChunked(true);
            request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            request.putHeader("Authorization", "Bearer " + userProps.getAccessToken());
            request.write(userProps.getKeycloakUser().encode()).end();
        });
    }

    public static boolean hasUserLevelRole(Role role) {
        return SQLUtils.in(role.toString(), Role.USER.toString(), Role.GUEST.toString());
    }

    public static boolean hasClientLevelRole(Role role) {
        return SQLUtils.in(role.toString(), Role.MANAGER.toString(), Role.USER.toString(), Role.GUEST.toString());
    }

    public static String getCompanyId(JsonObject user) {
        return user.getString("company_id");
    }

    public static Role getRole(JsonObject user) {
        return Role.valueOf(user.getString("role"));
    }

}
