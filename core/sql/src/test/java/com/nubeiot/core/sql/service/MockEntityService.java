package com.nubeiot.core.sql.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.mock.oneschema.Tables;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.AuthorDao;
import com.nubeiot.core.sql.mock.oneschema.tables.daos.BookDao;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Author;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Book;
import com.nubeiot.core.sql.mock.oneschema.tables.records.AuthorRecord;
import com.nubeiot.core.sql.mock.oneschema.tables.records.BookRecord;
import com.nubeiot.core.sql.service.MockEntityService.Metadata.AuthorMetadata;
import com.nubeiot.core.sql.service.MockEntityService.Metadata.BookMetadata;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

interface MockEntityService {

    @SuppressWarnings("unchecked")
    interface Metadata {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        final class AuthorMetadata implements SerialKeyEntity<Author, AuthorRecord, AuthorDao> {

            static final AuthorMetadata INSTANCE = new AuthorMetadata();

            @Override
            public boolean enableTimeAudit() { return false; }

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


        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        final class BookMetadata implements SerialKeyEntity<Book, BookRecord, BookDao> {

            static final BookMetadata INSTANCE = new BookMetadata();

            @Override
            public boolean enableTimeAudit() { return false; }

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


    final class AuthorService extends AbstractEntityService<Author, AuthorMetadata>
        implements EntityValidation<Author>, EntityTransformer {

        AuthorService(@NonNull EntityHandler entityHandler) {
            super(entityHandler);
        }

        @Override
        public AuthorMetadata context() {
            return AuthorMetadata.INSTANCE;
        }

        @Override
        public @NonNull EntityValidation validation() {
            return this;
        }

        @Override
        public Author onCreating(RequestData reqData) throws IllegalArgumentException {
            final Author author = EntityValidation.super.onCreating(reqData);
            Strings.requireNotBlank(author.getLastName(), "last_name is mandatory");
            if (Objects.isNull(author.getDateOfBirth())) {
                throw new IllegalArgumentException("date_of_birth is mandatory");
            }
            return author;
        }

    }


    final class BookService extends AbstractOneToManyEntityService<Book, BookMetadata> {

        BookService(@NonNull AbstractEntityHandler entityHandler) {
            super(entityHandler);
        }

        @Override
        public boolean enableFullResourceInCUDResponse() {
            return true;
        }

        @Override
        public BookMetadata context() {
            return BookMetadata.INSTANCE;
        }

        @Override
        public Map<String, Function<String, ?>> jsonFieldConverter() {
            return Collections.singletonMap(Tables.BOOK.AUTHOR_ID.getName().toLowerCase(), Functions.toInt());
        }

    }

}
