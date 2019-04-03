package com.nubeiot.core.http.base.event;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;

public class RestEventApiMetadataTest {

    private RestEventApiMetadata.Builder createBuilder(EventAction event, HttpMethod method) {
        return createBuilder(event, method, "/api/golds", "gold_id");
    }

    private RestEventApiMetadata.Builder createBuilder(String path, String... params) {
        return createBuilder(EventAction.GET_ONE, HttpMethod.GET, path, params);
    }

    private RestEventApiMetadata.Builder createBuilder(EventAction event, HttpMethod method, String path,
                                                       String... params) {
        return RestEventApiMetadata.builder()
                                   .action(event)
                                   .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                   .address("address.1")
                                   .method(method)
                                   .path(path)
                                   .paramNames(Arrays.asList(params));
    }

    @Test
    public void test_post() {
        RestEventApiMetadata metadata = createBuilder(EventAction.CREATE, HttpMethod.POST).build();
        Assert.assertEquals("/api/golds", metadata.getPath());
    }

    @Test
    public void test_get_list() {
        RestEventApiMetadata metadata = createBuilder(EventAction.GET_LIST, HttpMethod.GET).build();
        Assert.assertEquals("/api/golds", metadata.getPath());
    }

    @Test
    public void test_get_one() {
        RestEventApiMetadata metadata = createBuilder(EventAction.GET_ONE, HttpMethod.GET).build();
        Assert.assertEquals("/api/golds/:gold_id", metadata.getPath());
    }

    @Test
    public void test_other_method() {
        RestEventApiMetadata metadata = createBuilder(EventAction.INIT, HttpMethod.OPTIONS).build();
        Assert.assertEquals("/api/golds", metadata.getPath());
        Assert.assertEquals(HttpMethod.OPTIONS, metadata.getMethod());
    }

    @Test
    public void test_custom_gen_path() {
        RestEventApiMetadata metadata = createBuilder(EventAction.CREATE, HttpMethod.POST,
                                                      "/api/translate").generatePath(RestEventApiMetadata::rawPath)
                                                                       .build();
        Assert.assertEquals("/api/translate", metadata.getPath());
        Assert.assertEquals(HttpMethod.POST, metadata.getMethod());
        Assert.assertEquals(EventAction.CREATE, metadata.getAction());
        Assert.assertFalse(metadata.isPathPattern());
    }

    @Test
    public void test_combine_not_pattern() {
        RestEventApiMetadata metadata = createBuilder("/catalogue/products", "catalog_id", "product_type", "product_id")
                                            .pathPattern(false)
                                            .build();
        Assert.assertFalse(metadata.isPathPattern());
        Assert.assertNotNull(metadata.getGeneratePath());
        Assert.assertEquals("/catalogue/products/:catalog_id/:product_type/:product_id", metadata.getPath());
    }

    @Test
    public void test_combine_pattern() {
        RestEventApiMetadata metadata = createBuilder("/catalogue/{0}/products/type/{1}/product/{2}", "catalog_id",
                                                      "product_type", "product_id").pathPattern(true).build();
        Assert.assertTrue(metadata.isPathPattern());
        Assert.assertNotNull(metadata.getGeneratePath());
        Assert.assertEquals("/catalogue/:catalog_id/products/type/:product_type/product/:product_id",
                            metadata.getPath());
    }

}
