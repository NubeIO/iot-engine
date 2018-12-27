package com.nubeiot.core.micro;

import org.junit.Test;

public class MicroConfigTest {

    @Test
    public void test() {
        System.out.println(new MicroConfig().toJson().encodePrettily());
    }

}