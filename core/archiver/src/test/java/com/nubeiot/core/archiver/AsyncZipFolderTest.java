package com.nubeiot.core.archiver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.component.EventClientProxy;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.FileUtils;

@RunWith(VertxUnitRunner.class)
public class AsyncZipFolderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private EventbusClient client;
    private File origin;
    private File dest;

    @Before
    public void setup() throws IOException {
        final Vertx vertx = Vertx.vertx();
        client = EventClientProxy.create(vertx, new DeliveryOptions()).transporter();
        origin = folder.newFolder("origin");
        dest = folder.newFolder("dest");
    }

    @Test
    public void test_zip(TestContext context) throws IOException {
        folder.newFile(folder.getRoot().toPath().relativize(origin.toPath().resolve("abc.txt")).toString());
        folder.newFile(folder.getRoot().toPath().relativize(origin.toPath().resolve("def.txt")).toString());
        final Path xyz = origin.toPath().resolve("xyz");
        assert xyz.toFile().mkdirs();
        folder.newFile(folder.getRoot().toPath().relativize(xyz.resolve("ghj.txt")).toString());
        final String outFile = AsyncArchiver.ext(dest.toPath().resolve(origin.toPath().getFileName()));
        final ZipOutput expected = ZipOutput.builder()
                                            .inputPath(origin.toString())
                                            .outputPath(outFile)
                                            .size(384)
                                            .build();
        createNotifier(context, expected.toJson(), context.async(), null);
        AsyncZipFolder.builder()
                      .transporter(client)
                      .notifiedAddress("xxx")
                      .build()
                      .zip(ZipArgument.noTimestamp(), dest, origin);
    }

    @Test
    public void test_unzip(TestContext context) throws InterruptedException {
        final File zipFile = FileUtils.getClasspathFile("origin.zip").toFile();
        final ZipOutput expected = ZipOutput.builder()
                                            .inputPath(zipFile.toString())
                                            .outputPath(dest.toPath().resolve("origin").toString())
                                            .build();
        final Async async = context.async(2);
        final CountDownLatch latch = new CountDownLatch(1);
        createNotifier(context, expected.toJson(), async, latch);
        DefaultAsyncUnzip.builder()
                         .transporter(client)
                         .notifiedAddress("xxx")
                         .build()
                         .extract(ZipArgument.noTimestamp(), dest, zipFile);
        latch.await(TestHelper.TEST_TIMEOUT_SEC, TimeUnit.SECONDS);
        final Path extractFile = dest.toPath().resolve("origin").resolve("abc.txt");
        client.getVertx().fileSystem().exists(extractFile.toString(), event -> {
            context.assertTrue(event.succeeded());
            context.assertTrue(event.result());
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void test_unzip_not_found(TestContext context) {
        final Async async = context.async();
        final Path zipFile = dest.toPath().resolve("origin.zip");
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "java.io.FileNotFoundException: " + zipFile +
                                                                    " (The system cannot find the file " +
                                                                    "specified)");
        createNotifier(context, expected, async, null);
        DefaultAsyncUnzip.builder()
                         .transporter(client)
                         .notifiedAddress("xxx")
                         .build()
                         .extract(ZipArgument.noTimestamp(), dest.toPath(), zipFile);
    }

    private void createNotifier(TestContext context, JsonObject expected, Async async, CountDownLatch latch) {
        final TestZipNotifier notifier = new TestZipNotifier(context, async, expected, latch);
        client.register("xxx", notifier);
    }

}
