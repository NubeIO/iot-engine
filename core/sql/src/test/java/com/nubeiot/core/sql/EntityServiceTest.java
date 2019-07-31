package com.nubeiot.core.sql;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.sql.EntityService.SerialKeyEntity;
import com.nubeiot.core.sql.mock.oneschema.Tables;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.AuthorDao;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.BookDao;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Author;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Book;
import com.nubeiot.core.sql.mock.oneschema.tables.records.AuthorRecord;
import com.nubeiot.core.sql.mock.oneschema.tables.records.BookRecord;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class EntityServiceTest extends BaseSqlTest {

    private final String authorAddress = "com.nubeiot.core.sql.author";
    private final String bookAddress = "com.nubeiot.core.sql.book";

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) {
        super.before(context);
        MockOneEntityHandler entityHandler = startSQL(context, OneSchema.CATALOG, MockOneEntityHandler.class);
        controller().register(authorAddress, new AuthorService(entityHandler));
        controller().register(bookAddress, new BookService(entityHandler));
    }

    @Override
    @NonNull String getJdbcUrl() {
        return "jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString();
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test
    public void test_get_list_without_filter(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"authors\":[{\"id\":1,\"first_name\":\"George\",\"last_name\":\"Orwell\"," +
            "\"date_of_birth\":\"1903-06-26\",\"distinguished\":true},{\"id\":2,\"first_name\":\"Paulo\"," +
            "\"last_name\":\"Coelho\",\"date_of_birth\":\"1947-08-24\",\"distinguished\":false}]}");
        asserter(context, true, expected, authorAddress, EventAction.GET_LIST, RequestData.builder().build());
    }

    @Test
    public void test_get_list_with_filter(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"authors\":[{\"id\":1,\"first_name\":\"George\",\"last_name\":\"Orwell\"," +
            "\"date_of_birth\":\"1903-06-26\",\"distinguished\":true}]}");
        final RequestData reqData = RequestData.builder().filter(new JsonObject().put("first_name", "George")).build();
        asserter(context, true, expected, authorAddress, EventAction.GET_LIST, reqData);
    }

    @Test
    public void test_get_list_with_filter_no_result(TestContext context) {
        JsonObject expected = new JsonObject("{\"authors\":[]}");
        final RequestData reqData = RequestData.builder().filter(new JsonObject().put("first_name", "xxx")).build();
        asserter(context, true, expected, authorAddress, EventAction.GET_LIST, reqData);
    }

    @Test
    public void test_get_one(TestContext context) {
        JsonObject expected = new JsonObject("{\"id\":1,\"first_name\":\"George\",\"last_name\":\"Orwell\"," +
                                             "\"date_of_birth\":\"1903-06-26\",\"distinguished\":true}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("id", "1")).build();
        asserter(context, true, expected, authorAddress, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_get_one_not_found(TestContext context) {
        JsonObject expected = new JsonObject("{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with id=3\"}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("id", "3")).build();
        asserter(context, false, expected, authorAddress, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_create_one(TestContext context) {
        JsonObject expected = new JsonObject("{\"resource\":{\"id\":3,\"first_name\":\"ab\",\"last_name\":\"xyz\"," +
                                             "\"date_of_birth\":\"2019-07-30\",\"distinguished\":null}," +
                                             "\"action\":\"CREATE\",\"status\":\"SUCCESS\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new Author().setFirstName("ab")
                                                           .setLastName("xyz")
                                                           .setDateOfBirth(LocalDate.now())
                                                           .toJson())
                                         .build();
        asserter(context, true, expected, authorAddress, EventAction.CREATE, reqData);
    }

    @Test
    public void test_create_one_failed(TestContext context) {
        JsonObject expected = new JsonObject("{\"code\":\"INVALID_ARGUMENT\",\"message\":\"last_name is mandatory\"}");
        RequestData reqData = RequestData.builder().body(new Author().toJson()).build();
        asserter(context, false, expected, authorAddress, EventAction.CREATE, reqData);
    }

    @Test
    public void test_update_one(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject("{\"resource\":{\"id\":1,\"first_name\":\"ab\",\"last_name\":\"xyz\"," +
                                             "\"date_of_birth\":\"1980-03-07\",\"distinguished\":null}," +
                                             "\"action\":\"UPDATE\",\"status\":\"SUCCESS\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new Author().setId(1)
                                                           .setFirstName("ab")
                                                           .setLastName("xyz")
                                                           .setDateOfBirth(LocalDate.of(1980, 3, 8))
                                                           .toJson())
                                         .build();
        CountDownLatch latch = new CountDownLatch(1);
        asserter(context, true, expected, authorAddress, EventAction.UPDATE, reqData, latch);
        expected = new JsonObject("{\"id\":1,\"first_name\":\"ab\",\"last_name\":\"xyz\"," +
                                  "\"date_of_birth\":\"1980-03-07\",\"distinguished\":null}");
        reqData = RequestData.builder().body(new JsonObject().put("id", "1")).build();
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 4, TimeUnit.SECONDS);
        asserter(context, true, expected, authorAddress, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_patch_one(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject("{\"resource\":{\"id\":1,\"first_name\":\"ab\",\"last_name\":\"Orwell\"," +
                                             "\"date_of_birth\":\"1903-06-26\",\"distinguished\":true}," +
                                             "\"action\":\"PATCH\",\"status\":\"SUCCESS\"}");
        RequestData reqData = RequestData.builder().body(new Author().setId(1).setFirstName("ab").toJson()).build();
        CountDownLatch latch = new CountDownLatch(1);
        asserter(context, true, expected, authorAddress, EventAction.PATCH, reqData, latch);
        expected = new JsonObject("{\"id\":1,\"first_name\":\"ab\",\"last_name\":\"Orwell\"," +
                                  "\"date_of_birth\":\"1903-06-26\",\"distinguished\":true}");
        reqData = RequestData.builder().body(new JsonObject().put("id", "1")).build();
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 4, TimeUnit.SECONDS);
        asserter(context, true, expected, authorAddress, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_delete_one(TestContext context) {
        JsonObject expected = new JsonObject("{\"id\":1}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("id", "1")).build();
        asserter(context, true, expected, bookAddress, EventAction.REMOVE, reqData);
    }

    @Test
    public void test_delete_one_not_found(TestContext context) {
        JsonObject expected = new JsonObject("{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with id=3\"}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("id", "3")).build();
        asserter(context, false, expected, authorAddress, EventAction.REMOVE, reqData);
    }

    private void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address,
                          EventAction action, RequestData reqData) {
        asserter(context, isSuccess, expected, address, action, reqData, new CountDownLatch(1));
    }

    private void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address,
                          EventAction action, RequestData reqData, CountDownLatch latch) {
        final Async async = context.async();
        controller().request(address, EventPattern.REQUEST_RESPONSE, EventMessage.initial(action, reqData),
                             ReplyEventHandler.builder().action(action).success(msg -> {
                                 System.out.println(msg.toJson().encode());
                                 context.assertEquals(isSuccess, msg.isSuccess());
                                 JsonHelper.assertJson(context, async, expected,
                                                       isSuccess ? msg.getData() : msg.getError().toJson());
                                 latch.countDown();
                             }).build());
    }

    static final class AuthorService extends AbstractEntityService<Integer, Author, AuthorRecord, AuthorDao>
        implements SerialKeyEntity<Author, AuthorRecord, AuthorDao> {

        AuthorService(@NonNull EntityHandler entityHandler) {
            super(entityHandler);
        }

        @Override
        protected boolean enableTimeAudit() {
            return false;
        }

        @Override
        protected boolean enableFullResourceInCUDResponse() {
            return true;
        }

        @Override
        protected @NonNull String listKey() {
            return "authors";
        }

        @Override
        protected Author validateOnCreate(@NonNull Author pojo, @NonNull JsonObject headers)
            throws IllegalArgumentException {
            Strings.requireNotBlank(pojo.getLastName(), "last_name is mandatory");
            if (Objects.isNull(pojo.getDateOfBirth())) {
                throw new IllegalArgumentException("date_of_birth is mandatory");
            }
            return super.validateOnCreate(pojo, headers);
        }

        @Override
        public @NonNull Class<Author> modelClass() {
            return Author.class;
        }

        @Override
        public @NonNull Class<AuthorDao> daoClass() {
            return AuthorDao.class;
        }

        @Override
        public @NonNull JsonTable<AuthorRecord> table() {
            return Tables.AUTHOR;
        }

    }


    static final class BookService extends AbstractEntityService<Integer, Book, BookRecord, BookDao>
        implements SerialKeyEntity<Book, BookRecord, BookDao> {

        BookService(@NonNull EntityHandler entityHandler) {
            super(entityHandler);
        }

        @Override
        protected boolean enableTimeAudit() {
            return false;
        }

        @Override
        protected boolean enableFullResourceInCUDResponse() {
            return false;
        }

        @Override
        protected @NonNull String listKey() {
            return "books";
        }

        @Override
        public @NonNull Class<Book> modelClass() {
            return Book.class;
        }

        @Override
        public @NonNull Class<BookDao> daoClass() {
            return BookDao.class;
        }

        @Override
        public @NonNull JsonTable<BookRecord> table() {
            return Tables.BOOK;
        }

    }

}
