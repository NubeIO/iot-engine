package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.skyscreamer.jsonassert.Customization;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HttpUtils;

import lombok.Builder;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
public class ExpectedResponse {

    private final JsonObject expected;
    private final int code;
    private final List<Customization> customizations;
    private Consumer<ResponseData> after;

    public boolean hasAfter() {
        return Objects.nonNull(after);
    }

    public void _assert(@NonNull TestContext context, Async async, @NonNull ResponseData actual) {
        System.out.println("Response asserting...");
        System.out.println(actual.getStatus());
        try {
            context.assertEquals(HttpUtils.DEFAULT_CONTENT_TYPE,
                                 actual.headers().getString(HttpHeaders.CONTENT_TYPE.toString()));
            context.assertNotNull(actual.headers().getString("x-response-time"));
            context.assertEquals(code, actual.getStatus().code());
            JsonHelper.assertJson(context, async, expected, actual.body(), customizations);
            Optional.ofNullable(after).ifPresent(c -> c.accept(actual));
        } finally {
            TestHelper.testComplete(async);
        }
    }

    public static class Builder {

        Builder customizations(Customization... customizations) {
            this.customizations = Arrays.asList(customizations);
            return this;
        }

    }

}
