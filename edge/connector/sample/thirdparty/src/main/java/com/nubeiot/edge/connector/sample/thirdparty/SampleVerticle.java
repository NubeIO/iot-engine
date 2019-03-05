package com.nubeiot.edge.connector.sample.thirdparty;

import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;
import com.nubeiot.edge.connector.driverapi.DriverVerticle;

public class SampleVerticle extends ContainerVerticle {

    @Override
    public void start(Future<Void> future) {
        super.start(future);
        deployVerticle(DriverVerticle.class).flatMap(ignore -> deployVerticle(BACnetVerticle.class)).subscribe(s -> {
            logger.info("Deploy success");
            future.complete();
        }, throwable -> {
            logger.error("Failed", throwable);
            future.fail(throwable);
        });
    }

    private Single<String> deployVerticle(Class<? extends ContainerVerticle> verticleClass) {
        return Single.create(
            source -> getVertx().deployVerticle(verticleClass, new DeploymentOptions().setConfig(config()), r -> {
                // Deploy succeed
                if (r.succeeded()) {
                    source.onSuccess("Deployment of " + verticleClass + " is successful.");
                    logger.info("Deployment of {} is successful.", verticleClass);
                } else {
                    // Deploy failed
                    source.onError(r.cause());
                    logger.error("Cannot deploy {}", r.cause(), verticleClass);
                }
            }));
    }

}
