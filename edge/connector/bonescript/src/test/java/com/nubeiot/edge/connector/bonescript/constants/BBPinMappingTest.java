package com.nubeiot.edge.connector.bonescript.constants;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.TestBase;

public class BBPinMappingTest extends TestBase {

    @Test
    public void testGetAnalogInput() {
        BBPinMapping bbPinMapping = new BBPinMappingV14();
        Assert.assertEquals(7, bbPinMapping.getAnalogInPins().size());
    }

}
