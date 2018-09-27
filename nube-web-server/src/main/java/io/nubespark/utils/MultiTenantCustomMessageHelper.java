package io.nubespark.utils;


import io.nubespark.Role;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MultiTenantCustomMessageHelper {
    public static String buildAbsoluteUri(Message<Object> message, String location) {
        if (StringUtils.isNull(location)) {
            return "";
        }
        return "http://" + getHost(message) + location;
    }

    private static String getHost(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        return customMessage.getHeader().getString("host");
    }

    public static JsonObject getUser(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        return customMessage.getHeader().getJsonObject("user");
    }

    public static Role getRole(JsonObject user) {
        return Role.valueOf(user.getString("role"));
    }

    public static String getCompanyId(JsonObject user) {
        return user.getString("company_id");
    }

    public static String getAssociatedCompanyId(JsonObject user) {
        return user.getString("associated_company_id");
    }

    public static String getSiteId(JsonObject user) {
        return user.getString("site_id");
    }

    public static String getAccessToken(JsonObject user) {
        return user.getString("access_token");
    }

    public static JsonObject getBodyAsJson(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        return (JsonObject) customMessage.getBody();
    }

    public static JsonArray getBodyAsJsonArray(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        return (JsonArray) customMessage.getBody();
    }

    public static JsonObject getKeycloakConfig(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        return customMessage.getHeader().getJsonObject("keycloakConfig");
    }

    public static String getParamsId(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        String[] params = customMessage.getHeader().getString("url").split("/");
        if (params.length > 1) {
            return params[1];
        } else {
            return "";
        }
    }
}
