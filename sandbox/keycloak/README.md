# KeyCloak

## Script

```bash
docker run --rm -v /tmp/keycloak-export:/tmp/keycloak-export -v sandbox_keycloak-data:/opt/jboss/keycloak/standalone/data jboss/keycloak:4.8.3.Final -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=dir -Dkeycloak.migration.dir=/tmp/keycloak-export -Dkeycloak.migration.usersExportStrategy=SAME_FILE -Dkeycloak.migration.realmName=sandbox
```
