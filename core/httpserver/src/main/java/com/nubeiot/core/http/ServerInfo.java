package com.nubeiot.core.http;

import io.vertx.core.shareddata.Shareable;
import io.vertx.ext.web.Router;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ServerInfo.Builder.class)
public class ServerInfo implements JsonData, Shareable {

    private String host;
    private int port;
    private String publicHost;
    private String apiPath;
    private String wsPath;
    private String downloadPath;
    private String uploadPath;
    private String servicePath;
    private boolean ssl;
    private Router router;


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
