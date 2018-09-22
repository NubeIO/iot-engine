package io.nubespark.hive;

import io.nubespark.utils.Runner;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceBinder;

import java.net.URL;
import java.net.URLClassLoader;

import static io.nubespark.hive.HiveService.SERVICE_ADDRESS;
import static io.nubespark.hive.HiveService.SERVICE_NAME;

/**
 * Created by topsykretts on 4/26/18.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class HiveVerticle extends RxMicroServiceVerticle {

    private Logger logger = LoggerFactory.getLogger(HiveVerticle.class);

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-vertx-hive/src/main/java/";
        Runner.runExample(JAVA_DIR, HiveVerticle.class);
    }

    @Override
    public void start(Future<Void> startFuture) {
        super.start();
        logServerDetails();

        HiveService.create(vertx, config())
                .doOnSuccess(jdbcService -> {
                    ServiceBinder binder = new ServiceBinder(vertx.getDelegate());
                    binder.setAddress(SERVICE_ADDRESS).register(HiveService.class, jdbcService);
                    logger.info("Service bound to " + binder);
                })
                .flatMap(ignored -> publishMessageSource(SERVICE_NAME, SERVICE_ADDRESS))
                .subscribe(record -> startFuture.complete(), startFuture::fail);
    }

    private void logServerDetails() {
        logger.info("Config on Hive Engine app");
        logger.info(Json.encodePrettily(config()));

        logger.info("Classpath of Hive Engine app = " + System.getProperty("java.class.path"));
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        for (URL url : urls) {
            logger.info(url.getFile());
        }
        logger.info("Current thread loader = " + Thread.currentThread().getContextClassLoader());
        logger.info(HiveVerticle.class.getClassLoader());
    }

    protected Logger getLogger() {
        return logger;
    }
}
