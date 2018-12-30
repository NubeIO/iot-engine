#!/usr/bin/env bash

files=""
for var in "$@"
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