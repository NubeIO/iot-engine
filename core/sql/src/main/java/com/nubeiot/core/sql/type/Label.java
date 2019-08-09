package com.nubeiot.core.sql.type;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@JsonDeserialize(builder = Label.Builder.class)
@Builder(builderClassName = "Builder")
@ToString
public final class Label implements JsonData {

    private String label;
    private String description;


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
