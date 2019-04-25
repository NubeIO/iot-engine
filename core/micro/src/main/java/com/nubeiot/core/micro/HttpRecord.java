package com.nubeiot.core.micro;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder(builderClassName = "Builder")
@Getter
public class HttpRecord {

    @Default
    private String name = UUID.randomUUID().toString();
    @Default
    private boolean ssl = false;
    @Default
    private String host = "0.0.0.0";
    @Default
    private int port = 8080;
    @Default
    private String root = "";
    @Default
    private JsonObject metaData = new JsonObject();

}
