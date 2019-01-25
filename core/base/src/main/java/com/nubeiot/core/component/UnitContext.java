package com.nubeiot.core.component;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import lombok.Getter;

/**
 * Unit context after deployment
 *
 * @see Unit
 */
public class UnitContext {

    public static final UnitContext VOID = new UnitContext();

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Getter
    private String deployId;

    UnitContext registerDeployId(String deployId) {
        this.deployId = deployId;
        return this;
    }

}
