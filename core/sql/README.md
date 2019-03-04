# SQL Core

## Overview

Go through [Dev-SQL](https://github.com/NubeIO/iot-engine/wiki/Dev-%7C-SQL)

- Creates `sql ddl` file under `src/main/resources` to define your model.
- Define the mapping between sql and java data types, convertering between them in `dbTypes` of `build.gradle`.
- Setup java model generation in `task` of `build.gradle`.

## Current limitations

- Timestamp with timezone (`timestamptz`) data type is not supported.
- Function `CURRENT_TIMESTAMP` doesn't work with `PostgreSQL`.
