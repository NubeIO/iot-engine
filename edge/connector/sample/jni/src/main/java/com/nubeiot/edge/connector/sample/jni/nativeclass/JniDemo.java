package com.nubeiot.edge.connector.sample.jni.nativeclass;

import com.nubeiot.core.jni.JniBase;

public class JniDemo extends JniBase {

    public native void sayHello();

    public native void printSum(int a, int b);

    public native int getDefaultValue();

    public native int sum(int a, int b);
}
