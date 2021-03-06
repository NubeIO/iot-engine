package com.nubeiot.core.dto;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

public class JsonTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void inject_global_context_object() throws Exception {
        ContextObject ctx = new ContextObject();
        mapper.setInjectableValues(new InjectableValues.Std().addValue(ContextObject.class, ctx));
        DataNeedingContext data = mapper.readValue("{\"prop\":\"foo\"}", DataNeedingContext.class);
        assertThat(data.ctx, sameInstance(ctx));
        assertThat(data.prop, equalTo("foo"));
    }

    @Test
    public void inject_local_context_object() throws Exception {
        ContextObject ctx = new ContextObject();
        DataNeedingContext data = mapper.readerFor(DataNeedingContext.class)
                                        .with(new InjectableValues.Std().addValue(ContextObject.class, ctx))
                                        .readValue("{\"prop\":\"foo\"}");
        assertThat(data.ctx, sameInstance(ctx));
        assertThat(data.prop, equalTo("foo"));
    }

    public static class ContextObject {}


    @AllArgsConstructor
    public static class DataNeedingContext {

        private final ContextObject ctx;
        public String prop;

        private DataNeedingContext(ContextObject ctx) {
            this.ctx = ctx;
        }

        @JsonCreator
        public static DataNeedingContext create(@JacksonInject ContextObject ctx, @JsonProperty("prop") String prop) {
            return new DataNeedingContext(ctx, prop);
        }

    }

}
