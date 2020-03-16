package com.nubeiot.core.sql.service;

import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.sql.MockOneEntityHandler;
import com.nubeiot.core.sql.SchemaTest;
import com.nubeiot.core.sql.service.MockEntityService.AuthorService;
import com.nubeiot.core.sql.service.MockEntityService.BookService;

public abstract class DefaultSqlServiceTest extends BaseSqlServiceTest {

    static final String AUTHOR_ADDRESS = "com.nubeiot.core.sql.author";
    static final String BOOK_ADDRESS = "com.nubeiot.core.sql.book";
    protected MockOneEntityHandler entityHandler;

    protected void setup(TestContext context) {
        entityHandler = startSQL(context, SchemaTest.OneSchema.CATALOG, MockOneEntityHandler.class);
        controller().register(AUTHOR_ADDRESS, new AuthorService(entityHandler))
                    .register(BOOK_ADDRESS, new BookService(entityHandler));
    }

}
