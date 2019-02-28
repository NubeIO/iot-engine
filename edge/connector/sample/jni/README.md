# Edge JNI sample
This module is mock `edge` service:

- Create Library
- Call Library functions with the help of `:core:jni` `Provider` 

### How To Create Library

- ***Edit gradle.properties as your requirement (IMPORTANT):***
   - `jni.include.dir`: Java SDK where JNI file resides
   - `system.include.dir`: System dir where we can find headers files
   - `local.include.dir`: Local dir where we can put our custom headers files
- Create Java class with native function (it must be on inside folder `nativeclass`)
- Generate header file with command:
   - `gradle nativeHeadersGen`
- Create C/C++ file using the generated header file, jni.h and required others.
- Generate lib file using following command:
   - `gradle nativeLibsGen` (by default it will generate library on directory `~/.nubeio/`, which is the fallback 
   libDir of JniConfig)

To generate headers files as well as the libraries: `gradle nativeGen`

Reference link:
   - https://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html 

### Use Library

- Example: `EdgeJniDemo`
- Config Example: `resources/config.json` *(edit the config as your requirement)*
