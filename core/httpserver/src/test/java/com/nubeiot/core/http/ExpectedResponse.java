package com.nubeiot.core.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.skyscreamer.jsonassert.Customization;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HttpUtils;

import lombok.Builder;

@Builder(builderClassName = "Builder")
public class ExpectedResponse {

    private final JsonObject expected;
    private final int statusCode;
    private final List<Customization> customizations;
    private Consumer<ResponseData> after;

    public void _assert(TestContext context, Async async, ResponseData actual) {
        System.out.println("Client asserting...");
        System.out.println(actual.getStatus());
        try {
            context.assertEquals(HttpUtils.DEFAULT_CONTENT_TYPE,
                                 actual.headers().getString(HttpHeaders.CONTENT_TYPE.toString()));
            context.assertNotNull(actual.headers().getString("x-response-time"));
            context.assertEquals(statusCode, actual.getStatus().code());
            JsonHelper.assertJson(context, async, expected, actual.body(), Optional.ofNullable(customizations)
                                                                                   .orElse(new ArrayList<>())
                                                                                   .toArray(new Customization[] {}));
            Optional.ofNullable(after).ifPresent(c -> c.accept(actual));
        } catch (AssertionError e) {
            context.fail(e);
        }
    }

    public static class Builder {

        Builder customizations(Customization... customizations) {
            this.customizations = Arrays.asList(customizations);
            return this;
        }

    }

}
