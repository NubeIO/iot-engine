package com.nubeiot.core.http.rest;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;

public class RestEventMetadataTest {

    private RestEventMetadata.Builder createBuilder(EventAction event, HttpMethod method) {
        return createBuilder(event, method, "/api/gold", "gold_id");
    }

    private RestEventMetadata.Builder createBuilder(String path, String... params) {
        return createBuilder(EventAction.GET_ONE, HttpMethod.GET, path, params);
    }

    private RestEventMetadata.Builder createBuilder(EventAction event, HttpMethod method, String path,
                                                    String... params) {
        return RestEventMetadata.builder()
                                .action(event).pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                .address("address.1")
                                .method(method).path(path).paramNames(Arrays.asList(params));
    }

    @Test
    public void test_post() {
        RestEventMetadata metadata = createBuilder(EventAction.CREATE, HttpMethod.POST).build();
        Assert.assertEquals("/api/golds", metadata.getPath());
    }

    @Test
    public void test_get_list() {
        RestEventMetadata metadata = createBuilder(EventAction.GET_LIST, HttpMethod.GET).build();
        Assert.assertEquals("/api/golds", metadata.getPath());
    }

    @Test
    public void test_get_one() {
        RestEventMetadata metadata = createBuilder(EventAction.GET_ONE, HttpMethod.GET).build();
        Assert.assertEquals("/api/gold/:gold_id", metadata.getPath());
    }

    @Test
    public void test_other_method() {
        RestEventMetadata metadata = createBuilder(EventAction.INIT, HttpMethod.OPTIONS).build();
        Assert.assertEquals("/api/gold", metadata.getPath());
        Assert.assertEquals(HttpMethod.OPTIONS, metadata.getMethod());
    }

    @Test
    public void test_custom_gen_path() {
        RestEventMetadata metadata = createBuilder(EventAction.CREATE, HttpMethod.POST, "/api/translate").generatePath(
            RestEventMetadata::rawPath).local(true).build();
        Assert.assertEquals("/api/translate", metadata.getPath());
        Assert.assertEquals(HttpMethod.POST, metadata.getMethod());
        Assert.assertEquals(EventAction.CREATE, metadata.getAction());
        Assert.assertTrue(metadata.isLocal());
        Assert.assertFalse(metadata.isPathPattern());
    }

    @Test
    public void test_combine_not_pattern() {
        RestEventMetadata metadata = createBuilder("/catalogue/products", "catalog_id", "product_type",
                                                   "product_id").pathPattern(false).build();
        Assert.assertFalse(metadata.isPathPattern());
        Assert.assertNotNull(metadata.getGeneratePath());
        Assert.assertEquals("/catalogue/products/:catalog_id/:product_type/:product_id", metadata.getPath());
    }

    @Test
    public void test_combine_pattern() {
        RestEventMetadata metadata = createBuilder("/catalogue/{0}/products/type/{1}/product/{2}", "catalog_id",
                                                   "product_type", "product_id").pathPattern(true).build();
        Assert.assertTrue(metadata.isPathPattern());
        Assert.assertNotNull(metadata.getGeneratePath());
        Assert.assertEquals("/catalogue/:catalog_id/products/type/:product_type/product/:product_id",
                            metadata.getPath());
    }

}
