package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.MockEntityService.Metadata.AuthorMetadata;
import com.nubeiot.core.sql.MockEntityService.Metadata.BookMetadata;
import com.nubeiot.core.sql.mock.oneschema.Tables;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.AuthorDao;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.BookDao;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Author;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Book;
import com.nubeiot.core.sql.mock.oneschema.tables.records.AuthorRecord;
import com.nubeiot.core.sql.mock.oneschema.tables.records.BookRecord;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

class MockEntityService {

    static final class Metadata {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static final class AuthorMetadata implements SerialKeyEntity<Author, AuthorRecord, AuthorDao> {

            static final AuthorMetadata INSTANCE = new AuthorMetadata();

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

            @Override
            @NonNull
            public String listKey() {
                return "authors";
            }

        }


        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static final class BookMetadata implements SerialKeyEntity<Book, BookRecord, BookDao> {

            static final BookMetadata INSTANCE = new BookMetadata();

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

            @Override
            @NonNull
            public String listKey() {
                return "books";
            }

        }

    }


    static final class AuthorService
        extends AbstractEntityService<Integer, Author, AuthorRecord, AuthorDao, AuthorMetadata> {

        AuthorService(@NonNull EntityHandler entityHandler) {
            super(entityHandler);
        }

        @Override
        public boolean enableTimeAudit() {
            return false;
        }

        @Override
        public boolean enableFullResourceInCUDResponse() {
            return true;
        }

        @Override
        public AuthorMetadata metadata() {
            return AuthorMetadata.INSTANCE;
        }

        @Override
        public Author validateOnCreate(@NonNull Author pojo, @NonNull JsonObject headers)
            throws IllegalArgumentException {
            Strings.requireNotBlank(pojo.getLastName(), "last_name is mandatory");
            if (Objects.isNull(pojo.getDateOfBirth())) {
                throw new IllegalArgumentException("date_of_birth is mandatory");
            }
            return super.validateOnCreate(pojo, headers);
        }

    }


    static final class BookService extends AbstractEntityService<Integer, Book, BookRecord, BookDao, BookMetadata>
        implements OneToManyReferenceEntityService<Integer, Book, BookRecord, BookDao, BookMetadata> {

        BookService(@NonNull EntityHandler entityHandler) {
            super(entityHandler);
        }

        @Override
        public boolean enableTimeAudit() {
            return false;
        }

        @Override
        public boolean enableFullResourceInCUDResponse() {
            return true;
        }

        @Override
        public BookMetadata metadata() {
            return BookMetadata.INSTANCE;
        }

        @Override
        public Map<String, Function<String, ?>> jsonFieldConverter() {
            return Collections.singletonMap(Tables.BOOK.AUTHOR_ID.getName().toLowerCase(), Functions.toInt());
        }

    }

}
