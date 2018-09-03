package io.nubespark.utils;

import io.vertx.core.json.JsonObject;

/**
 * Custom message for example
 *
 * @author Junbong
 */
public class CustomMessage<T> {
    private final JsonObject header;
    private final T body;
    private final JsonObject user;
    private final int statusCode;

    public CustomMessage(JsonObject header, T body, JsonObject user, int statusCode) {
        this.header = header;
        this.body = body;
        this.user = user;
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CustomMessage{");
        sb.append("header=").append(header);
        sb.append(" ,body=").append(body).append('\'');
        sb.append(" ,user=").append(user).append('\'');
        sb.append(" ,statusCode=").append(statusCode).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public JsonObject getHeader() {
        return header;
    }

    public T getBody() {
        return body;
    }

    public JsonObject getUser() {
        return user;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
