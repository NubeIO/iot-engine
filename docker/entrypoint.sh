#!/bin/bash

if [[ ${NUBE_CLUSTER_PUBLIC} == "true" ]]; then
    JAVA_PROPS="$JAVA_PROPS -Dnube.cluster.public=true"
    JAVA_PROPS="$JAVA_PROPS -Dnube.cluster.public.host=$NUBE_CLUSTER_PUBLIC_HOST"
    JAVA_PROPS="$JAVA_PROPS -Dnube.cluster.public.port=$NUBE_CLUSTER_PUBLIC_PORT"
    JAVA_PROPS="$JAVA_PROPS -Dnube.cluster.public.eventbus.port=$NUBE_CLUSTER_PUBLIC_EVENTBUS_PORT"
fi

java ${JVM_OPTS} ${JAVA_PROPS} -jar ${ARTIFACT}.jar -conf conf/config.json
