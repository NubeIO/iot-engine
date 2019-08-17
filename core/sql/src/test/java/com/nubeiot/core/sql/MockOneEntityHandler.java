package com.nubeiot.core.sql;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.AuthorDao;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.BookDao;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.BookToBookStoreDao;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.LanguageDao;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Author;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Book;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.BookToBookStore;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Language;

import lombok.Getter;

/**
 * @see <a href="http://www.jooq.org/doc/3.11/manual-single-page/#sample-database">sample-database</a>
 */
@Getter
public class MockOneEntityHandler extends AbstractEntityHandler {

    private final LanguageDao languageDao;
    private final AuthorDao authorDao;
    private final BookDao bookDao;
    private final BookToBookStoreDao bookStoreDao;

    public MockOneEntityHandler(Configuration jooqConfig, Vertx vertx) {
        super(jooqConfig, vertx);
        this.authorDao = dao(AuthorDao.class);
        this.languageDao = dao(LanguageDao.class);
        this.bookDao = dao(BookDao.class);
        this.bookStoreDao = dao(BookToBookStoreDao.class);
    }

    @Override
    public boolean isNew() {
        return isNew(com.nubeiot.core.sql.mock.oneschema.tables.Author.AUTHOR);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Single<EventMessage> initData() {
        Single<Integer> insert00 = languageDao.insert(lang());
        Single<Integer> insert01 = authorDao.insert(author());
        Single<Integer> insert02 = bookDao.insert(book());
        //TODO Recheck it
        //        Single<Integer> insert03 = bookStoreDao.insert(bookStore(), true);
        return Single.concatArray(insert00, insert01, insert02)
                     .reduce(0, Integer::sum)
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.success(EventAction.MIGRATE));
    }

    private List<Language> lang() {
        return Arrays.asList(new Language().setId(1).setCd("en").setDescription("English"),
                             new Language().setId(2).setCd("de").setDescription("Deutsch"),
                             new Language().setId(3).setCd("fr").setDescription("Français"),
                             new Language().setId(4).setCd("pt").setDescription("Português"));
    }

    private List<Author> author() {
        Author author = new Author(1, "George", "Orwell", LocalDate.of(1903, 6, 26), true);
        Author author1 = new Author(2, "Paulo", "Coelho", LocalDate.of(1947, 8, 24), false);
        return Arrays.asList(author, author1);
    }

    private List<Book> book() {
        Book book = new Book(1, 1, "1984", OffsetDateTime.of(1947, 12, 31, 17, 1, 0, 0, ZoneOffset.UTC), 1);
        Book book1 = new Book(2, 1, "Animal Farm", OffsetDateTime.of(1944, 12, 31, 17, 1, 0, 0, ZoneOffset.UTC), 1);
        Book book2 = new Book(3, 2, "O Alquimista", OffsetDateTime.of(1987, 12, 31, 18, 1, 0, 0, ZoneOffset.UTC), 4);
        Book book3 = new Book(4, 2, "Brida", OffsetDateTime.of(1989, 12, 31, 18, 1, 0, 0, ZoneOffset.UTC), 2);
        return Arrays.asList(book, book1, book2, book3);
    }

    List<BookToBookStore> bookStore() {
        BookToBookStore bs = new BookToBookStore("Orell Füssli", 1, 10);
        BookToBookStore bs1 = new BookToBookStore("Orell Füssli", 2, 10);
        BookToBookStore bs2 = new BookToBookStore("Orell Füssli", 3, 10);
        BookToBookStore bs3 = new BookToBookStore("Ex Libris", 1, 1);
        BookToBookStore bs4 = new BookToBookStore("Ex Libris", 3, 2);
        BookToBookStore bs5 = new BookToBookStore("Buchhandlung im Volkshaus", 3, 1);
        return Arrays.asList(bs, bs1, bs2, bs3, bs4, bs5);
    }

}
