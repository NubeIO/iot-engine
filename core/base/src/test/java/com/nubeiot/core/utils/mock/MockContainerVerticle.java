package com.nubeiot.core.utils.mock;

import com.nubeiot.core.component.ContainerVerticle;

import lombok.Getter;
import lombok.Setter;

public final class MockContainerVerticle extends ContainerVerticle {

    @Getter
    @Setter
    private boolean error;

    @Override
    public void start() {
        logger.info("Starting Mock Container Verticle...");
        super.start();
        if (error) {
            throw new RuntimeException();
        }
    }

    @Override
    public String configFile() {
        return "mock-container.json";
    }

}
