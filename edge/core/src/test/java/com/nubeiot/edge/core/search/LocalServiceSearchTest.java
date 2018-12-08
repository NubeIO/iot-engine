package com.nubeiot.edge.core.search;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.edge.core.model.gen.Tables;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalServiceSearchTest {

    @Test
    public void testStateENABLED() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("state", State.ENABLED.name());
        JsonObject validatedFilter = service.validateFilter(filter);
        assertEquals(validatedFilter.getValue(Tables.TBL_MODULE.STATE.getName()), State.ENABLED.name());
    }

    @Test
    public void testStateNONE() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("state", State.NONE.name());
        JsonObject validatedFilter = service.validateFilter(filter);
        assertEquals(validatedFilter.getValue(Tables.TBL_MODULE.STATE.getName()), State.NONE.name());
    }

    @Test
    public void testStatePENDING() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("state", State.PENDING.name());
        JsonObject validatedFilter = service.validateFilter(filter);
        assertEquals(validatedFilter.getValue(Tables.TBL_MODULE.STATE.getName()), State.PENDING.name());
    }

    @Test
    public void testStateDISABLED() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("state", State.DISABLED.name());
        JsonObject validatedFilter = service.validateFilter(filter);
        assertEquals(validatedFilter.getValue(Tables.TBL_MODULE.STATE.getName()), State.DISABLED.name());
    }

    @Test
    public void testStateUNAVAILABLE() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("state", State.UNAVAILABLE.name());
        JsonObject validatedFilter = service.validateFilter(filter);
        assertEquals(validatedFilter.getValue(Tables.TBL_MODULE.STATE.getName()), State.UNAVAILABLE.name());
    }

    @Test(expected = NubeException.class)
    public void testStateINVALID() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("state", "INVALID");
        service.validateFilter(filter);

    }

    @Test
    public void testFromDate() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("from_" , "2018-11-16T20:10:22+06:00");
        JsonObject validatedFilter = service.validateFilter(filter);
        assertTrue(validatedFilter.containsKey("from"));
    }

    @Test
    public void testToDate() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("to", "2018-11-16T20:10:22+06:00");
        JsonObject validatedFilter = service.validateFilter(filter);
        assertTrue(validatedFilter.containsKey("to"));
    }

    @Test(expected = NubeException.class)
    public void testInvalidDate() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put("to", "2018-11-16T20:10:22 06:00");
        service.validateFilter(filter);
    }

    @Test
    public void testParamsFilter() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put(Tables.TBL_MODULE.DEPLOY_ID.getName(), "1");
        filter.put(Tables.TBL_MODULE.SERVICE_ID.getName(), "2");
        filter.put(Tables.TBL_MODULE.SERVICE_NAME.getName(), "3");
        filter.put(Tables.TBL_MODULE.SERVICE_TYPE.getName(), "4");
        filter.put(Tables.TBL_MODULE.VERSION.getName(), "5");
        filter.put(Tables.TBL_MODULE.CREATED_AT.getName(), "6");
        filter.put(Tables.TBL_MODULE.DEPLOY_CONFIG_JSON.getName(), "7");
        filter.put(Tables.TBL_MODULE.MODIFIED_AT.getName(), "8");
        filter.put(Tables.TBL_MODULE.PUBLISHED_BY.getName(), "9");
        JsonObject validatedFilter = service.validateFilter(filter);
        assertFalse(filter.equals(validatedFilter));
    }
    
    @Test
    public void testParams() {
        LocalServiceSearch service = new LocalServiceSearch(null);
        JsonObject filter = new JsonObject();
        filter.put(Tables.TBL_MODULE.DEPLOY_ID.getName(), "1");
        filter.put(Tables.TBL_MODULE.SERVICE_ID.getName(), "2");
        filter.put(Tables.TBL_MODULE.SERVICE_NAME.getName(), "3");
        filter.put(Tables.TBL_MODULE.SERVICE_TYPE.getName(), "4");
        filter.put(Tables.TBL_MODULE.VERSION.getName(), "5");
        filter.put(Tables.TBL_MODULE.DEPLOY_CONFIG_JSON.getName(), "7");
        filter.put(Tables.TBL_MODULE.PUBLISHED_BY.getName(), "9");
        JsonObject validatedFilter = service.validateFilter(filter);
        assertFalse(filter.equals(validatedFilter));
    }

}
