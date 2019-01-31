#!/usr/bin/env bash

java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseContainerSupport -XX:MaxRAM=375m -XX:MaxRAMPercentage=75 -Xss128k -XX:+UseSerialGC -XX:+TieredCompilation -XX:TieredStopAtLevel=1
