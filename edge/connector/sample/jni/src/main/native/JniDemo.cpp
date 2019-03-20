#include <jni.h>
#include <iostream>
#include "../../../generated/main/native/headers/com_nubeiot_edge_connector_sample_jni_nativeclass_JniDemo.h"

using namespace std;

// Implementation of the native method sayHello()
JNIEXPORT void JNICALL Java_com_nubeiot_edge_connector_sample_jni_nativeclass_JniDemo_sayHello
  (JNIEnv *, jobject) {
   cout << "Hello World!\n" << endl;
}

JNIEXPORT void JNICALL Java_com_nubeiot_edge_connector_sample_jni_nativeclass_JniDemo_printSum
  (JNIEnv *, jobject, jint a, jint b) {
  int sum = a + b;
  cout << "Sum is: " << sum << endl;
}

JNIEXPORT jint JNICALL Java_com_nubeiot_edge_connector_sample_jni_nativeclass_JniDemo_getDefaultValue
  (JNIEnv *, jobject) {
  int defaultValue = 5;
  return defaultValue;
}

JNIEXPORT jint JNICALL Java_com_nubeiot_edge_connector_sample_jni_nativeclass_JniDemo_sum
  (JNIEnv *, jobject, jint a, jint b) {
    return a + b;
}
