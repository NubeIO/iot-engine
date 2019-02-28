#!/usr/bin/env bash

files=""
dashboard="dashboard mongo keycloak ditto postgres"
edge="edge nexus kafka"
ditto="mongo"

stack="$@"
[[ ${stack} == *"dashboard"* ]] && stack="$dashboard $stack"
[[ ${stack} == *"edge"* ]] && stack="$edge $stack"
[[ ${stack} == *"ditto"* ]] && stack="$ditto $stack"

#`ip route get 8.8.8.8 | awk '{print $3; exit}'`
stack=$(echo "$stack" | awk '{for (i=1;i<=NF;i++) if (!a[$i]++) printf("%s%s",$i,FS)}{printf("\n")}')

for var in ${stack}
do
    file="$var-docker-compose.yml"
    if [[ -e "$file" ]]; then
        files="$files -f $file"
    else
        echo "File $file does not exist"
    fi
done

CMD="docker-compose $files up"
echo ${CMD}
eval ${CMD}
