package com.nubeiot.core.http.base.event;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.base.Urls;

public class RestEventApiMetadataTest {

    private RestEventApiMetadata.Builder createBuilder(EventAction event, HttpMethod method) {
        return createBuilder(event, method, "/api/golds", "/:gold_id");
    }

    private RestEventApiMetadata.Builder createBuilder(String path, String... params) {
        return createBuilder(EventAction.GET_ONE, HttpMethod.GET, path, params);
    }

    private RestEventApiMetadata.Builder createBuilder(EventAction action, HttpMethod method, String path,
                                                       String... params) {

        final ActionMethodMapping mapping = createMapping(action, method);
        return RestEventApiMetadata.builder()
                                   .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                   .address("address.1")
                                   .definition(
                                       EventMethodDefinition.create(path, Urls.capturePath("/", params), mapping));
    }

    private ActionMethodMapping createMapping(EventAction action, HttpMethod method) {
        return new ActionMethodMapping() {
            @Override
            public Map<EventAction, HttpMethod> get() {
                return Collections.singletonMap(action, method);
            }
        };
    }

    @Test
    public void test_post() {
        RestEventApiMetadata metadata = createBuilder(EventAction.CREATE, HttpMethod.POST).build();
        Assert.assertEquals(EventAction.CREATE, metadata.getDefinition().search("/api/golds", HttpMethod.POST));
        Assert.assertEquals(1, metadata.getDefinition().getMapping().size());
    }

    @Test
    public void test_get_list() {
        RestEventApiMetadata metadata = createBuilder(EventAction.GET_LIST, HttpMethod.GET).build();
        Assert.assertEquals(EventAction.GET_LIST, metadata.getDefinition().search("/api/golds", HttpMethod.GET));
    }

    @Test
    public void test_get_one() {
        RestEventApiMetadata metadata = createBuilder("/api/golds", "gold_id").build();
        Assert.assertEquals(EventAction.GET_ONE, metadata.getDefinition().search("/api/golds/xxx", HttpMethod.GET));
    }

    @Test
    public void test_custom_gen_path() {
        RestEventApiMetadata metadata = createBuilder(EventAction.CREATE, HttpMethod.POST, "/api/translate").build();
        Assert.assertEquals(EventAction.CREATE, metadata.getDefinition().search("/api/translate", HttpMethod.POST));
    }

    @Test
    public void test_combine_multiple_param_sequential() {
        EventMethodDefinition def = EventMethodDefinition.create("/catalogue/products",
                                                                 "/:catalog_id/:product_type/:product_id",
                                                                 createMapping(EventAction.GET_ONE, HttpMethod.GET));
        RestEventApiMetadata metadata = RestEventApiMetadata.builder()
                                                            .address("address.1")
                                                            .pattern(EventPattern.REQUEST_RESPONSE)
                                                            .definition(def)
                                                            .build();
        Assert.assertEquals(EventAction.GET_ONE,
                            metadata.getDefinition().search("/catalogue/products/123/456/777", HttpMethod.GET));
    }

    @Test
    public void test_combine_pattern() {
        EventMethodDefinition def = EventMethodDefinition.create(
            "/catalogue/:catalog_id/products/type/:product_type/product", "/:product_id",
            createMapping(EventAction.GET_ONE, HttpMethod.GET));
        RestEventApiMetadata metadata = RestEventApiMetadata.builder()
                                                            .address("address.1")
                                                            .pattern(EventPattern.REQUEST_RESPONSE)
                                                            .definition(def)
                                                            .build();
        Assert.assertEquals(EventAction.GET_ONE, metadata.getDefinition()
                                                         .search("/catalogue/123/products/type/456/product/xyz",
                                                                 HttpMethod.GET));
    }

}
