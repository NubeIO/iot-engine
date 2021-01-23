package com.nubeiot.edge.connector.bacnet.simulator;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.Configs;

public class SimulatorConfigTest {

    @Test
    public void deserialize() {
        SimulatorConfig fromFile = IConfig.from(Configs.loadJsonConfig("bacnetTestConfig.json"), SimulatorConfig.class);
        Assert.assertNotNull(fromFile);
        Assert.assertEquals("NubeIOEdge28TEST", fromFile.getDeviceName());
        Assert.assertEquals(654321, fromFile.getDeviceId());

        Assert.assertNotNull(fromFile.getNetworks());
        Assert.assertEquals(2, fromFile.getNetworks().size());
        fromFile.getNetworks().toNetworks().forEach(ipConfig -> {
            Assert.assertNotNull(ipConfig);
            Assert.assertFalse(ipConfig.getLabel().isEmpty());
        });
    }

}
