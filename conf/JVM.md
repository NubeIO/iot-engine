# Java Virtual Machine Tuning

**Note**:

```math
JVM Memory Usage = Heap + Stack per thread (XSS) * Threads + Constant Overhead
```

## Useful Resources

- https://www.oracle.com/technetwork/java/hotspotfaq-138619.html
- https://www.oracle.com/technetwork/java/javase/8u191-relnotes-5032181.html#JDK-8146115
- https://medium.com/adorsys/jvm-memory-settings-in-a-container-environment-64b0840e1d9e

## Default config

```bash
# kb
java -XX:+PrintFlagsFinal -version | grep ThreadStackSize

# bytes
java -XX:+PrintFlagsFinal -version | grep -Ei "heap|max"
```

```bash
java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseContainerSupport -XX:+UseSerialGC -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:ActiveProcessorCount=0 -XX:MaxRAM=395m -XX:MinRAMPercentage=50.0 -XX:MaxRAMPercentage=80.0 -Xss256k -XX:+PrintFlagsFinal -version | grep -Ei "heap|max|ThreadStackSize"
```

## Useful options

| JVM Argument                                                                              | Effect                                                                                                                                                                                                                                                       |
| ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| -XX:+UseSerialGC                                                                          | Uses only 1 GC thread. This both limits the cpu instruction count for garbage collection, but also results in the minimal memory footprint.                                                                                                                  |
| -XX:ParallelGCThreads=n                                                                   | Set the number of parallel GC threads to n. This is helpful if you are trying to limit the cpu usage of your container, or if you are running with a JVM that doesn't include the patch to calculate GC threads based on processors available to the CGroup. |
| -XX:MaxRAM                                                                                | Sets the maximum amount of memory used by the JVM to n, where n may be expressed in terms of megabytes 100m or gigabytes 2g.                                                                                                                                 |
| -XX:+UseCGroupMemoryLimitForHeap                                                          | This flag present in the more recent builds of JDK8 tells the JVM to use the information in /sys/fs/cgroup/memory/memory.limit_in_bytes to calculate memory defaults.                                                                                        |
| -XX:+TieredCompilation -XX:TieredStopAtLevel=1                                            | Turns off the optimizing compiler. This can sometimes decrease the footprint of your running JVM.                                                                                                                                                            |
| -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40                                           | These parameters tell the heap to shrink aggressively and to grow conservatively. Thereby optimizing the amount of memory available to the operating system.                                                                                                 |
| -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90                                         | These parameters are necessary when running parallel GC if you want to use the Min and Max Heap Free ratios.                                                                                                                                                 |
| -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking="summary" -XX:+PrintNMTStatistics | These options will print out the non-heap memory usage of your JVM.                                                                                                                                                                                          |
| -Xss256k                                                                                  | This will decrease the size of your Java Stacks.                                                                                                                                                                                                             |
