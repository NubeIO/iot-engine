package com.nubeiot.edge.connector.bonescript.constants;

import java.util.Set;

// Edge iO BB 28 V1.5
public class BBPinMappingV15 extends BBPinMapping {

    @Override
    public Set<String> getAnalogInPins() {
        return getFieldsKey(AnalogIn.class.getDeclaredFields());
    }

    @Override
    public Set<String> getAnalogOutPins() {
        return getFieldsKey(AnalogOut.class.getDeclaredFields());
    }

    @Override
    public Set<String> getDigitalInPins() {
        return getFieldsKey(DigitalIn.class.getDeclaredFields());
    }

    @Override
    public Set<String> getDigitalOutPins() {
        return getFieldsKey(DigitalOut.class.getDeclaredFields());
    }

    public static class AnalogIn {

        public static String UI1 = "P9_39";
        public static String UI2 = "P9_40";
        public static String UI3 = "P9_37";
        public static String UI4 = "P9_38";
        public static String UI5 = "P9_33";
        public static String UI6 = "P9_36";
        public static String UI7 = "P9_35";

    }


    public static class AnalogOut {

        public static String UO1 = "P8_13";
        public static String UO2 = "P9_14";
        public static String UO3 = "P9_21";
        public static String UO4 = "P9_42";
        public static String UO5 = "P8_19";
        public static String UO6 = "P9_16";
        public static String UO7 = "P9_22";

    }


    public static class DigitalIn {

        public static String DI1 = "P9_30";
        public static String DI2 = "P9_15";
        public static String DI3 = "P9_31";
        public static String DI4 = "P9_28";
        public static String DI5 = "P9_23";
        public static String DI6 = "P9_25";
        public static String DI7 = "P9_27";

    }


    public static class DigitalOut {

        public static String DO1 = "P8_7";
        public static String DO2 = "P8_8";
        public static String DO3 = "P8_9";
        public static String DO4 = "P8_10";
        public static String DO5 = "P8_12";
        public static String R1 = "P9_29";
        public static String R2 = "P9_12";

    }

}
