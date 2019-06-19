# io.vertx Dependency Bundle

   
Issue [#141](https://github.com/NubeIO/iot-engine/issues/141): Bug related to dependency resolving.

`io.vertx` classes are only loaded by main classloader not [isolated classloader](https://vertx.io/docs/vertx-core/java/#_verticle_isolation_groups) i.e. the verticles that are 
deployed 
dynamically using [Maven verticle factory](https://vertx.io/docs/vertx-maven-service-factory/java/) cannot find `io.vertx` classes.

Ideally, this module loads all required `io.vertx` dependencies to `edge-bios` module main classpath (or `server-bios` if
 required).
The `bios` module should use following in its `build.gradle`:
 
``
compile project(':core:iovertx')
``

> If any new io.vertx dependencies are used by the project (mainly installable modules), then these dependencies must
 be added to `build.gradle` of `iovertx` module.

Alternative is to use  `compile "io.vertx:vertx-dependencies"`. However, there are many unnecessary dependencies.
