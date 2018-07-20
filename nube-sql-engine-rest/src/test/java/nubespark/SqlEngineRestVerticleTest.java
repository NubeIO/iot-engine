package nubespark;

import io.nubespark.SqlEngineRestVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
@RunWith(VertxUnitRunner.class)
public class SqlEngineRestVerticleTest {
    private Vertx vertx;
    private Integer port;

    /**Setting up the*/
    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(SqlEngineRestVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    /**Closing after completion*/
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    /**Testing particular */
    //check response

    @Test
    public void testDefaultResponseFromRunningVerticle(TestContext context){
        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
            response.handler(body -> {
                System.out.println("This is body " + body.toString());
                context.assertTrue(body.toString().contains("sql-engine-rest"));
                async.complete();
            });
        });
    }

    /**Testing API response*/
    // we can pass query params as {"query":"select * from metadata limit 10"} in json string and check the response
    //but since it need JDBC driver its test is limited for now.
    @Test
    public void testSQLQueryFilter(TestContext context) {
        Async async = context.async();
        final String json = "{}";
        final String length = Integer.toString(json.length());
        vertx.createHttpClient().post(port, "localhost", "/engine")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 400);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        System.out.println("This is response from server : " + body.toString());
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }
}
