package io.nubespark;

import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

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
        router.route().handler(BodyHandler.create());

        BridgeOptions options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("news-feed"))
                .addOutboundPermitted(new PermittedOptions().setAddress("io.nubespark.ditto.events"));


        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options, event -> {

            // You can also optionally provide a handler like this which will be passed any events that occur on the bridge
            // You can use this for monitoring or logging, or to change the raw messages in-flight.
            // It can also be used for fine grained access control.

            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                System.out.println("A socket was created");
            }

            // This signals that it's ok to process the event
            event.complete(true);

        }));

        //creating static resource handler
        router.route().handler(StaticHandler.create());

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
