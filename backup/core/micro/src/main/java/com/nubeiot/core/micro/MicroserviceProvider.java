package com.nubeiot.core.micro;

import com.nubeiot.core.component.UnitProvider;

public final class MicroserviceProvider implements UnitProvider<Microservice> {

    @Override
    public Microservice get() { return new Microservice(); }

    @Override
    public Class<Microservice> unitClass() { return Microservice.class; }

}
