package com.nubeiot.core.dto;

import java.io.Serializable;
import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface DataTransferObject extends Serializable, JsonData {

    @JsonProperty(value = "body")
    JsonObject body();

    @JsonProperty(value = "headers")
    JsonObject headers();

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class Headers {

        /**
         * For tracking created/modified by user
         */
        public static final String X_REQUEST_USER = "X-Request-User";

        /**
         * For tracking request by service
         */
        public static final String X_REQUEST_BY = "X-Request-By";

        /**
         * For tracking forwarded for request by service
         */
        public static final String X_REQUEST_FORWARDED_FOR = "X-Forwarded-For";

        /**
         * For tracking correlation message in distributed message system
         */
        //TODO Add Correlation_Id system
        public static final String X_CORRELATION_ID = "X-Correlation-ID";

    }


    @SuppressWarnings("unchecked")
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    abstract class AbstractDTO implements DataTransferObject {

        private JsonObject headers = new JsonObject();
        private JsonObject body = new JsonObject();

        AbstractDTO(JsonObject headers, JsonObject body) {
            this.headers = Objects.nonNull(headers) ? headers : new JsonObject();
            this.body = body;
        }

        @Override
        public final JsonObject body() { return body; }

        @Override
        public final JsonObject headers() { return headers; }

        public <T extends AbstractDTO> T setBody(JsonObject body) {
            this.body = body;
            return (T) this;
        }

        public <T extends AbstractDTO> T setHeaders(JsonObject headers) {
            this.headers = headers;
            return (T) this;
        }

    }

}
