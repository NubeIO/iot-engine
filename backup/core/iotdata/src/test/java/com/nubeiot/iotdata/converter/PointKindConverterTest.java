package com.nubeiot.iotdata.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.nubeiot.iotdata.dto.PointKind;

public class PointKindConverterTest {

    @Test
    public void testConverter() {
        final PointKindConverter converter = new PointKindConverter();
        assertEquals("INPUT", converter.to(PointKind.INPUT));
        assertEquals(PointKind.UNKNOWN, converter.from(null));
        assertEquals(PointKind.OUTPUT, converter.from("output"));
    }

}
