#!/bin/sh

[[ $CLUSTER_PUBLIC == "true" ]] && JAVA_PROPS="$JAVA_PROPS -Dcluster.public.addr=`ip route get 8.8.8.8 | awk '{print $3; exit}'`:$CLUSTER_PUBLIC_PORT"
java ${JVM_OPTS} ${JAVA_PROPS} -jar ${ARTIFACT}.jar -conf conf/config.json
