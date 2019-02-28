#include <jni.h>
#include <stdio.h>
#include "../../../generated/main/native/headers/com_nubeiot_edge_connector_sample_jni_nativeclass_JniDemo.h"

// Implementation of the native method sayHello()
JNIEXPORT void JNICALL Java_com_nubeiot_edge_connector_sample_jni_nativeclass_JniDemo_sayHello
  (JNIEnv *, jobject) {
   printf("Hello World!\n");
   return;
}
