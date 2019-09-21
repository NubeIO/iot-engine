package com.nubeiot.core.sql.service;

import java.util.List;

import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.mock.oneschema.Keys;
import com.nubeiot.core.sql.mock.oneschema.Tables;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Author;
import com.nubeiot.core.sql.mock.oneschema.tables.records.BookRecord;
import com.nubeiot.core.sql.service.MockEntityService.Metadata.AuthorMetadata;
import com.nubeiot.core.sql.service.MockEntityService.Metadata.BookMetadata;

@RunWith(VertxUnitRunner.class)
public class CoreEntityServiceTest extends BaseSqlServiceTest {

    @Test
    public void test_get_references(TestContext context) {
        final List<Table> tables = entityHandler.referencesTo(BookMetadata.INSTANCE.table());
        context.assertEquals(1, tables.size());
        context.assertEquals(Tables.BOOK_TO_BOOK_STORE, tables.get(0));
    }

    @Test
    public void test_get_referenceKeys(TestContext context) {
        final List<ForeignKey> fks = entityHandler.referenceKeysTo(BookMetadata.INSTANCE.table());
        context.assertEquals(1, fks.size());
        context.assertEquals(Keys.FK_B2BS_BOOK, fks.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_get_linked_data(TestContext context) {
        final List<ForeignKey> fks = entityHandler.referenceKeysTo(AuthorMetadata.INSTANCE.table());
        context.assertEquals(1, fks.size());
        context.assertEquals(Keys.FK_BOOK_AUTHOR, fks.get(0));
        TableRecord with = new BookRecord().setAuthorId(1);
        with.attach(entityHandler.getJooq());
        Author record = fks.get(0).fetchParent(with).into(Author.class);
        context.assertNotNull(record);
        context.assertEquals(1, record.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_get_linked_not_found(TestContext context) {
        final List<ForeignKey> fks = entityHandler.referenceKeysTo(AuthorMetadata.INSTANCE.table());
        context.assertEquals(1, fks.size());
        context.assertEquals(Keys.FK_BOOK_AUTHOR, fks.get(0));
        TableRecord with = new BookRecord().setAuthorId(5);
        with.attach(entityHandler.getJooq());
        context.assertNull(fks.get(0).fetchParent(with));
    }

}
