package com.nubeiot.edge.connector.bonescript;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.utils.Strings;

public class DataInitTest extends TestBase {

    @Test
    public void testInitDittoTemplate() {
        Assert.assertTrue(Strings.isNotBlank(Init.initDittoTemplate().toString()));
    }

}
