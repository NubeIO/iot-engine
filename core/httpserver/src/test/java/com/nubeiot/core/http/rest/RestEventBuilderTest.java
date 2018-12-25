package com.nubeiot.core.http.rest;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.mock.MockApiDefinition;

public class RestEventBuilderTest {

    @Test(expected = InitializerError.class)
    public void test_no_register_data() {
        new RestEventBuilder().validate();
    }

    @Test
    public void test_root_api() {
        Assert.assertEquals("/api", new RestEventBuilder().getRootApi());
        Assert.assertEquals("/api", new RestEventBuilder().rootApi("").getRootApi());
        Assert.assertEquals("/api", new RestEventBuilder().rootApi(null).getRootApi());
        Assert.assertEquals("/xyz", new RestEventBuilder().rootApi("xyz").getRootApi());
    }

    @Test(expected = NullPointerException.class)
    public void test_register_null() {
        new RestEventBuilder().register((Class<RestEventApi>) null);
    }

    @Test
    public void test_register_one_api() {
        Set<Class<? extends RestEventApi>> validate = new RestEventBuilder().register(
                MockApiDefinition.MockRestEventApi.class).validate();
        Assert.assertEquals(1, validate.size());
    }

    @Test
    public void test_register_many_same_api() {
        @SuppressWarnings("unchecked")
        Set<Class<? extends RestEventApi>> validate = new RestEventBuilder().register(
                MockApiDefinition.MockRestEventApi.class, MockApiDefinition.MockRestEventApi.class).validate();
        Assert.assertEquals(1, validate.size());
    }

}