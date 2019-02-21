#!/usr/bin/env bash

files=""

for var in "$@"
do
    file="$var-docker-compose.yml"
    if [[ -e "$file" ]]; then
        files="$files -f $file"
        if [[ $1 == "dashboard" ]]; then
            files="$files -f mongo-docker-compose.yml -f keycloak-docker-compose.yml"
        elif [[ $1 == "edge" ]]; then
            files="$files -f nexus-docker-compose.yml -f kafka-docker-compose.yml"
        fi
    else
        echo "File $file does not exist"
    fi
done

CMD="docker-compose $files up"
echo ${CMD}
eval ${CMD}
