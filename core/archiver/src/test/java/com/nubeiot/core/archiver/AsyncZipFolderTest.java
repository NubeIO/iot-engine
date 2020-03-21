package com.nubeiot.core.archiver;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.component.EventClientProxy;
import com.nubeiot.core.event.EventbusClient;

@RunWith(VertxUnitRunner.class)
public class AsyncZipFolderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private EventbusClient client;

    @Before
    public void setup() {
        final Vertx vertx = Vertx.vertx();
        client = EventClientProxy.create(vertx, new DeliveryOptions()).transporter();
    }

    @Test
    public void test(TestContext context) throws IOException {
        final File dest = folder.newFolder("destination");
        final File origin = folder.newFolder("origin");
        folder.newFile(folder.getRoot().toPath().relativize(origin.toPath().resolve("abc.txt")).toString());
        final ZipOutput expected = ZipOutput.builder()
                                            .originFile(origin.toString())
                                            .zipFile(AsyncZip.ext(dest.toPath().resolve(origin.toPath().getFileName())))
                                            .size(216)
                                            .build();
        final TestZipNotifier notifier = new TestZipNotifier(context, context.async(), expected.toJson());
        client.register("xxx", notifier);
        AsyncZipFolder.builder()
                      .transporter(client)
                      .notifiedAddress("xxx")
                      .zipArgument(ZipArgument.noTimestamp())
                      .build()
                      .run(dest, origin);
    }

}
