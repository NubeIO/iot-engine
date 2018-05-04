package io.nubespark;

import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Created by topsykretts on 5/4/18.
 */
public class HttpServerVerticle extends MicroServiceVerticle {

    public static final String SERVICE_NAME = "io.nubespark.frontend.server";

    @Override
    public void start() {
        super.start();
        //// TODO: 5/4/18 Use SockJs to make event bus available

        //// TODO: 5/5/18 Implement backend logic of users, roles, authentication, business logic end points

        Router router = Router.router(vertx);
        // creating body handler
        router.route("/*").handler(BodyHandler.create());

        //creating static resource handler
        router.route("/*").handler(StaticHandler.create());

        //By default index.html from webroot/ is available on "/".
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8080),
                        handler -> {
                            if (handler.failed()) {
                                handler.cause().printStackTrace();
                                Future.failedFuture(handler.cause());
                            } else {
                                System.out.println("Front End server started...");
                                Future.succeededFuture();
                            }
                        }
                );
        publishHttpEndpoint(SERVICE_NAME, config().getString("host", "localhost"),
                config().getInteger("http.port", 8080), ar-> {
                    if (ar.failed()) {
                        ar.cause().printStackTrace();
                    } else {
                        System.out.println("Front End Server service published : " + ar.succeeded());
                    }
                });
    }
}
