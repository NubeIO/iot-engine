package com.nubeiot.edge.connector.sample.thirdparty;

import com.nubeiot.edge.connector.driverapi.DriverVerticle;

public class DriverApiVerticleTest extends DriverVerticle {

    @Override
    public String configFile() { return "gateway.json"; }

}
