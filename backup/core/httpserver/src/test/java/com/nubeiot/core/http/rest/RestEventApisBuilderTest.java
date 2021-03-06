package com.nubeiot.core.http.rest;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.mock.MockApiDefinition;

public class RestEventApisBuilderTest {

    @Test(expected = InitializerError.class)
    public void test_no_register_data() {
        new RestEventApisBuilder().validate();
    }


    @Test(expected = NullPointerException.class)
    public void test_register_null() {
        new RestEventApisBuilder().register((Class<RestEventApi>) null);
    }

    @Test
    public void test_register_one_api() {
        Set<Class<? extends RestEventApi>> validate = new RestEventApisBuilder().register(
            MockApiDefinition.MockRestEventApi.class).validate();
        Assert.assertEquals(1, validate.size());
    }

    @Test
    public void test_register_many_same_api() {
        Set<Class<? extends RestEventApi>> validate = new RestEventApisBuilder().register(
            MockApiDefinition.MockRestEventApi.class, MockApiDefinition.MockRestEventApi.class).validate();
        Assert.assertEquals(1, validate.size());
    }

}
