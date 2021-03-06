package com.nubeiot.core.micro.transfomer;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Status;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Getter
@FieldNameConstants
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = RecordOutput.Builder.class)
public class RecordOutput implements JsonData {

    private final String registration;
    private final String name;
    private final String type;
    private final Status status;
    private final String location;
    private final JsonArray endpoints;
    private final JsonObject metadata;


    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {}

}
