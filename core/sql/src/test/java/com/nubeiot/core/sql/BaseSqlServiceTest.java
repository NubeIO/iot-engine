package com.nubeiot.core.sql;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.sql.MockEntityService.AuthorService;
import com.nubeiot.core.sql.MockEntityService.BookService;

import lombok.NonNull;

abstract class BaseSqlServiceTest extends BaseSqlTest {

    static final String AUTHOR_ADDRESS = "com.nubeiot.core.sql.author";
    static final String BOOK_ADDRESS = "com.nubeiot.core.sql.book";

    @Before
    public void before(TestContext context) {
        super.before(context);
        MockOneEntityHandler entityHandler = startSQL(context, OneSchema.CATALOG, MockOneEntityHandler.class);
        controller().register(AUTHOR_ADDRESS, new AuthorService(entityHandler));
        controller().register(BOOK_ADDRESS, new BookService(entityHandler));
    }

    @NonNull
    public String getJdbcUrl() { return "jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString(); }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address, EventAction action,
                  RequestData reqData) {
        asserter(context, isSuccess, expected, address, action, reqData, new CountDownLatch(1));
    }

    void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address, EventAction action,
                  RequestData reqData, CountDownLatch latch) {
        final Async async = context.async();
        controller().request(address, EventPattern.REQUEST_RESPONSE, EventMessage.initial(action, reqData),
                             ReplyEventHandler.builder().action(action).success(msg -> {
                                 System.out.println(msg.toJson().encode());
                                 context.assertEquals(isSuccess, msg.isSuccess());
                                 JsonHelper.assertJson(context, async, expected, isSuccess
                                                                                 ? Objects.requireNonNull(msg.getData())
                                                                                 : msg.getError().toJson());
                                 latch.countDown();
                             }).build());
    }

}
