package io.nubespark;

import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceBinder;

import java.net.URL;
import java.net.URLClassLoader;

import static io.nubespark.JDBCService.SERVICE_ADDRESS;
import static io.nubespark.JDBCService.SERVICE_NAME;

/**
 * Created by topsykretts on 4/26/18.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class JdbcVerticle extends RxMicroServiceVerticle {

    private Logger logger = LoggerFactory.getLogger(JdbcVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {
        super.start();
        logServerDetails();

        final JDBCService jdbcService = JDBCService.create(vertx, config(), ready -> {
            if (ready.succeeded()) {
                ServiceBinder binder = new ServiceBinder(vertx.getDelegate());
                binder.setAddress(SERVICE_ADDRESS).register(JDBCService.class, ready.result());
                startFuture.complete();
            } else {
                startFuture.fail(ready.cause());
            }
        });

        publishMessageSource(SERVICE_NAME, SERVICE_ADDRESS)
                .subscribe(record -> startFuture.complete(), startFuture::fail);
        vertx.eventBus().consumer(SERVICE_ADDRESS, message -> {
            String query = message.body().toString();
            JsonObject queryObj = new JsonObject(query);
            String sqlQuery = queryObj.getString("query");
            JsonArray params = queryObj.getJsonArray("params");
            System.out.println("Received query : " + query);
            jdbcService.executeQueryWithParams(sqlQuery, params, resultHandler -> {
                message.reply(resultHandler.result());
            });
        });
    }

    private void logServerDetails() {
        logger.info("Config on JDBC Engine app");
        logger.info(Json.encodePrettily(config()));

        logger.info("Classpath of JDBC Engine app = " + System.getProperty("java.class.path"));
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        for (URL url : urls) {
            logger.info(url.getFile());
        }
        logger.info("Current thread loader = " + Thread.currentThread().getContextClassLoader());
        logger.info(JdbcVerticle.class.getClassLoader());
    }

    protected Logger getLogger() {
        return logger;
    }
}
