package com.nubeiot.edge.connector.sample.jni;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.jni.JniContext;
import com.nubeiot.core.jni.JniProvider;
import com.nubeiot.edge.connector.sample.jni.nativeclass.JniDemo;

public class EdgeJniDemo extends ContainerVerticle {

    private JniDemo jniDemo = new JniDemo();

    @Override
    public void start() {
        super.start();
        this.addProvider(new JniProvider(), this::handler);
    }

    private void handler(JniContext ignored) {
        jniDemo.sayHello();
    }

}
