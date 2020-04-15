package io.github.zero.jpa;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.github.zero.jpa.Sortable.Direction;
import io.github.zero.jpa.Sortable.Order;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SortableTest {

    @Test
    public void test_serialize_order() throws IOException, JSONException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String orderJson = objectMapper.writeValueAsString(Order.by("xyz", Direction.ASC));
        System.out.println(orderJson);
        final Order order = objectMapper.readValue(orderJson, Order.class);
        System.out.println(order);
        JSONAssert.assertEquals(orderJson, objectMapper.writeValueAsString(order), true);
    }

}
