package com.nubeiot.buildscript.jooq;

import io.github.jklingsporn.vertx.jooq.generate.builder.VertxGeneratorBuilder;

public final class ReactivePgJdbcGenerator extends NubeJdbcGenerator {

    public ReactivePgJdbcGenerator() {
        super(VertxGeneratorBuilder.init().withRXAPI().withJDBCDriver());
    }

}
