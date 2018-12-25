package com.nubeiot.core.http.ws;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderClassName = "Builder")
public class WebsocketEventMessage implements Serializable {

    @EqualsAndHashCode.Include
    private final String address;
    @JsonDeserialize(using = BridgeEventTypeDeserialize.class)
    @JsonSerialize(using = BridgeEventTypeSerialize.class)
    private final BridgeEventType type;
    private final EventMessage body;

    @JsonCreator
    private WebsocketEventMessage(@JsonProperty(value = "address", required = true) String address,
                                  @JsonProperty(value = "type", required = true) BridgeEventType type,
                                  @JsonProperty(value = "body") EventMessage body) {
        try {
            this.address = Strings.requireNotBlank(address);
            this.type = Objects.requireNonNull(type);
            this.body = body;
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InitializerError("Create " + WebsocketEventMessage.class.getSimpleName() + " object failure", e);
        }
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public static WebsocketEventMessage from(Object object) {
        try {
            JsonObject entries = object instanceof String
                                 ? new JsonObject((String) object)
                                 : JsonObject.mapFrom(Objects.requireNonNull(object));
            return entries.mapTo(WebsocketEventMessage.class);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Invalid websocket event body format",
                                    new HiddenException(e));
        }
    }

}
