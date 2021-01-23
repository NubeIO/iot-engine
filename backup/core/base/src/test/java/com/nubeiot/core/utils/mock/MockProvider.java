package com.nubeiot.core.utils.mock;

import com.nubeiot.core.component.MockUnitVerticle;
import com.nubeiot.core.component.UnitProvider;

import lombok.Getter;
import lombok.Setter;

public class MockProvider implements UnitProvider<MockUnitVerticle> {

    @Getter
    @Setter
    private MockUnitVerticle unitVerticle;

    @Override
    public Class<MockUnitVerticle> unitClass() { return MockUnitVerticle.class; }

    @Override
    public MockUnitVerticle get() { return unitVerticle; }

}
