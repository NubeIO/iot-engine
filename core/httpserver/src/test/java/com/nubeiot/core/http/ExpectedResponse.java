package com.nubeiot.core.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HttpUtils;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
public class ExpectedResponse {

    @NonNull
    private final JsonObject expected;
    private final int code;
    @Default
    @NonNull
    private final List<Customization> customizations = new ArrayList<>();
    @Default
    @NonNull
    private final JSONCompareMode mode = JSONCompareMode.STRICT;
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
            if (customizations.isEmpty()) {
                JsonHelper.assertJson(context, async, expected, actual.body(), mode);
            } else {
                JsonHelper.assertJson(context, async, expected, actual.body(), customizations);
            }
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
