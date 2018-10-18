package com.nubeio.iot.share.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PaginationTest {

    @Test
    public void test_build_without_value() {
        Pagination pagination = Pagination.builder().build();
        assertEquals(1, pagination.getPage());
        assertEquals(20, pagination.getPerPage());
    }

    @Test
    public void test_build_with_value() {
        Pagination pagination = Pagination.builder().page(5).perPage(15).build();
        assertEquals(5, pagination.getPage());
        assertEquals(15, pagination.getPerPage());
    }

    @Test
    public void test_build_with_per_page_greater() {
        Pagination pagination = Pagination.builder().perPage(50).build();
        assertEquals(1, pagination.getPage());
        assertEquals(20, pagination.getPerPage());
    }

    @Test
    public void test_build_with_per_page_equals_zero() {
        Pagination pagination = Pagination.builder().perPage(0).build();
        assertEquals(1, pagination.getPage());
        assertEquals(20, pagination.getPerPage());
    }

    @Test
    public void test_build_with_page_equals_zero() {
        Pagination pagination = Pagination.builder().page(0).build();
        assertEquals(1, pagination.getPage());
        assertEquals(20, pagination.getPerPage());
    }

}