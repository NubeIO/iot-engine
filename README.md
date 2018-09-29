# Nube  vert.x Engine

Using vert.x framework, to build an engine that allows remote installation of modules built using vert.x.

Use case: Remotely install/update/uninstall modules on IOT edge devices or edge servers.

## Architecture

- App developers use Maven to write apps and install apps (jars) to central maven repo.

- A Nube App Store in cloud is responsible to send commands of install/update/uninstall, manage the jars, provide statistics and analytics about usage, etc.

- A Nube App Installer in edge devices is responsible to install/update/uninstall according to Nube App Store’s command and report status for statistics.

Following is the rough architecture.

![nube-vertx-engine architecture](docs/nube-app-store.png?raw=true "Architecture of Nube vert.x Engine")


## nube-vertx-engine 

- Parent project that will be used by developers to build modules.
- It has some configurations and conventions to be used by sub-modules.

## nube-vertx-common
- Common code that will be used across all modules (Eg: Abstract Class, Parent Class, Cluster Config).
- It can be used as dependency by sub modules.

## nube-app-installer

- Install/Uninstall/Update apps/modules by managing dependency from central maven repository.
- Report usage and statistics

## nube-app-store-rest

- Nube App Store REST API for remotely installing/uninstalling/updating apps/modules. 
- Nube App Store REST API to upgrade Nube App Installer in remote instance.
- Statistics / Analytics of used apps (TODO).
- Tracking and managing connected remote instances (TODO).

## nube-bios

- Nube BIOS with Java 8 should be run on remote edge instance/device with clustering setting.
- Installs default version of Nube App Installer on startup using maven.
- Handles upgradation of Nube App Installer.

## Modules

- Developed by developers according to business logic and requirement.
- Deployed in central maven repository after development and testing.

### nube-vertx-hive
- A module for connecting FiloDB server and executing SQL operations on it.
- We have given a sql-hive/engine POST service available for sending SQL command.

### nube-vertx-hive
- A module for connecting PostgreSQL server and executing SQL operations on it.
- We have given a sql-pg/engine POST service available for sending SQL command.

### nube-vertx-mongo
- It makes the services available of MongoDB as the REST request.

### nube-vertx-zeppelin
- Facilitates the hive connection from our Gateway.

### nube-server-ditto-driver
- Connects to the ditto-server and listens its web-socket and published that into our EventBus.

### nube-edge-ditto-driver (Demo)
- Just a demo for edge devices 


## Running in localhost cluster

**Assumptions:**

 - Java 8 and Maven 3 are installed.
 
 - `<project_root_directory>/nube-vertx-common/src/main/resources/cluster.xml` contains hazelcast cluster config.
 
 
 ```
 <join>
   <multicast enabled="false"/>
   <tcp-ip enabled="true">
     <interface>127.0.0.1</interface>
   </tcp-ip>
 </join>
   <interfaces enabled="false">
   <interface>192.168.1.*</interface>
 </interfaces>
 ```
 
 - All `cd` are from `<project_base_directory>`.


1. Go to project root directory and install project in local repo (~/.m2).
`mvn clean install`

2. Run Nube App Store REST
```
cd nube-app-store-rest
java -jar target/nube-app-store-rest-1.0-SNAPSHOT-fat.jar -conf src/conf/config.json -cluster -cluster-host 127.0.0.1
```

3. Run Nube BIOS
```
cd nube-bios
java -jar target/nube-bios-1.0-SNAPSHOT-fat.jar -cluster -cluster-host 127.0.0.1
```

4. Check Logs and verify if both services are started correctly. Also, check if Nube App Store REST API is working.
`GET http://localhost:8086`

Response:
```
{
"name": "nubespark app store REST API",
"version": "1.0",
"vert.x_version": "3.4.1",
"java_version": "8.0"
}
```


## Running in distributed cluster

**Assumptions:**

 - Java 8 and Maven 3 are installed.
 
 - `<project_root_directory>/nube-vertx-common/src/main/resources/cluster.xml` contains hazelcast cluster config.
 
 
 ```
 <join>
   <multicast enabled="true"/>
   <tcp-ip enabled="false">
     <interface>127.0.0.1</interface>
   </tcp-ip>
 </join>
 <interfaces enabled="true">
   <interface>192.168.1.*</interface>
 </interfaces>
 ```
 > [Note: Interfaces (192.168.1.*) should be changed according to your network]
 
 - All `cd` are from `<project_base_directory>`.
 
1. We need to install central maven repository. There are options like Nexus, JFrog Artifactory, etc.
[Nexus](https://help.sonatype.com/repomanager3/installation) is used for test purpose which will be started on localhost:8081.
A `settings.xml` should be added to `~/.m2/` in development environment
```
<settings>
  <mirrors>
    <mirror>
      <!--This sends everything else to /public -->
      <id>nexus</id>
      <mirrorOf>*</mirrorOf>
      <url>http://localhost:8081/repository/maven-public/</url>
    </mirror>
  </mirrors>
  <profiles>
    <profile>
      <id>nexus</id>
      <!--Enable snapshots for the built in central repo to direct -->
      <!--all requests to nexus via the mirror -->
      <repositories>
        <repository>
          <id>central</id>
          <url>http://central</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>central</id>
          <url>http://central</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <activeProfiles>
    <!--make the profile active all the time -->
    <activeProfile>nexus</activeProfile>
  </activeProfiles>
  <servers>
    <server>
      <id>nexus</id>
      <username>admin</username>
      <password>admin123</password>
    </server>
  </servers>
</settings>
```

1. Go to project root directory and deploy the project in central repo.
`mvn clean deploy`

2. Run Nube App Store REST
```
cd nube-app-store-rest
java -jar target/nube-app-store-rest-1.0-SNAPSHOT-fat.jar -conf src/conf/config.json -cluster -cluster-host 192.168.1.68
```
> [Note: By default cluster.xml in nube-vertx-common project will be used but a custom cluster.xml can be added to classpath using argument `-cp ./your-directory-cluster` or `-Dvertx.hazelcast.config=./your-directory-cluster/cluster.xml`]

3. Run Nube BIOS
```
cd nube-bios
java -jar target/nube-bios-1.0-SNAPSHOT-fat.jar -cluster -cluster-host 192.168.1.70
```
> [Note: cluster-host for bios is 192.168.1.70 whereas Nube App Store REST is 192.168.1.68 i.e. these are running on different nodes]

4. Check Logs and verify if both services are started correctly. Also, check if Nube App Store REST API is working.
`GET http://192.168.1.68:8086`

### Install App

```
POST /api/store/install
{
  "groupId":"io.nubespark",
  "artifactId":"nube-jdbc-engine",
  "version":"1.0-SNAPSHOT"
}
```

### Uninstall App
```
POST /api/store/uninstall
{
 "artifactId":"nube-jdbc-engine"
}
```
