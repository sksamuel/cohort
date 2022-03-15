# Cohort

![main](https://github.com/sksamuel/cohort/workflows/main/badge.svg)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.cohort/cohort-core.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Ccohort)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.cohort/cohort-core.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/cohort/)

Cohort is a [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) style
replacement for non Spring services. It provides HTTP endpoints to help monitor and manage apps in production. For
example health, logging, database and JVM metrics.

## How to use

Include the core dependency `com.sksamuel.cohort:cohort-core:<version>` and the ktor
dependency `com.sksamuel.cohort:cohort-ktor:<version>` in your build along with the additional modules for the features
you wish to activate.

Then to wire into Ktor, we would install the `Cohort` plugin, and enable whichever features we want to expose. **By
default all features are disabled**.

Here is an example with each feature enabled.

```kotlin
install(Cohort) {

  // enable an endpoint to display operating system name and version
  operatingSystem = true

  // enable runtime JVM information such as vm options and vendor name
  jvmInfo = true

  // configure the Logback log manager to show effective log levels and allow runtime adjustment
  logManager = LogbackManager

  // show connection pool information
  dataSourceManager = HikariDataSourceManager(writerDs, readerDs)

  // show current system properties
  sysprops = true

  // enable an endpoint to dump the heap
  heapdump = true

  // enable healthchecks for kubernetes
  healthcheck("/liveness", livechecks)
  healthcheck("/readiness", readychecks)
}
```

## Healthchecks

Cohort provides health checks for a variety of JVM metrics such as memory and thread deadlocks as well as connectivity
to services such as Kafka and Elasticsearch and databases.

The kubelet uses liveness probes to know when to restart a container. For example, liveness probes could catch a
deadlock, where an application is running, but unable to make progress. Restarting a container in such a state can help
to make the application more available despite bugs.

The kubelet uses readiness probes to know when a container is ready to start accepting traffic. A Pod is considered
ready when all of its containers are ready. One use of this signal is to control which Pods are used as backends for
Services. When a Pod is not ready, it is removed from Service load balancers.

The kubelet uses startup probes to know when a container application has started. If such a probe is configured, it
disables liveness and readiness checks until it succeeds, making sure those probes don't interfere with the application
startup. This can be used to adopt liveness checks on slow starting containers, avoiding them getting killed by the
kubelet before they are up and running.

Sometimes, applications are temporarily unable to serve traffic. For example, an application might need to load large
data or configuration files during startup, or depend on external services after startup. In such cases, you don't want
to kill the application, but you don't want to send it requests either. Kubernetes provides readiness probes to detect
and mitigate these situations. A pod with containers reporting that they are not ready does not receive traffic through
Kubernetes Services.

### Endpoints

Here is an example of output from a health check with a series of configured health checks.

```json
[
  {
    "name": "com.sksamuel.cohort.memory.FreememHealthCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.445932Z",
    "message": "Freemem is above threshold [433441040 >= 67108864]",
    "cause": null,
    "consecutiveSuccesses": 75,
    "consecutiveFailures": 0
  },
  {
    "name": "com.sksamuel.cohort.system.OpenFileDescriptorsHealthCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.429469Z",
    "message": "Open file descriptor count within threshold [209 <= 16000]",
    "cause": null,
    "consecutiveSuccesses": 25,
    "consecutiveFailures": 0
  },
  {
    "name": "com.sksamuel.cohort.memory.GarbageCollectionTimeCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:00:54.422194Z",
    "message": "GC Collection time was 0% [Max is 25]",
    "cause": null,
    "consecutiveSuccesses": 6,
    "consecutiveFailures": 0
  },
  {
    "name": "writer connections",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.445868Z",
    "message": "Database connections is equal or above threshold [8 >= 8]",
    "cause": null,
    "consecutiveSuccesses": 75,
    "consecutiveFailures": 0
  },
  {
    "name": "reader connections",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.445841Z",
    "message": "Database connections is equal or above threshold [8 >= 8]",
    "cause": null,
    "consecutiveSuccesses": 75,
    "consecutiveFailures": 0
  },
  {
    "name": "com.sksamuel.cohort.system.SystemCpuHealthCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:01:09.463421Z",
    "message": "System CPU is below threshold [0.12667261373773417 < 0.9]",
    "cause": null,
    "consecutiveSuccesses": 75,
    "consecutiveFailures": 0
  },
  {
    "name": "com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck",
    "healthy": true,
    "lastCheck": "2022-03-15T03:00:54.419733Z",
    "message": "There are 0 deadlocked threads",
    "cause": null,
    "consecutiveSuccesses": 6,
    "consecutiveFailures": 0
  }
]
```

## Logging

Cohort allows you to view the current logging configuration and update log levels at runtime.

Here is the example output of `/cohort/logging` which shows the current log levels:

## Jvm Info

Displays information about the JVM state, including VM options, JVM version, and vendor name.

```json
{
  "name": "106637@sam-H310M-A-2-0",
  "pid": 106637,
  "vmOptions": [
    "-Dvisualvm.id=32227655111670",
    "-javaagent:/home/sam/development/idea-IU-213.5744.125/lib/idea_rt.jar=36667:/home/sam/development/idea-IU-213.5744.125/bin",
    "-Dfile.encoding=UTF-8"
  ],
  "classPath": "/home/sam/development/workspace/......",
  "specName": "Java Virtual Machine Specification",
  "specVendor": "Oracle Corporation",
  "specVersion": "11",
  "vmName": "OpenJDK 64-Bit Server VM",
  "vmVendor": "AdoptOpenJDK",
  "vmVersion": "11.0.10+9",
  "startTime": 1647315704746,
  "uptime": 405278
}
```

## Operating System

Displays the running os and version.

```json
{
  "arch": "amd64",
  "name": "Linux",
  "version": "5.13.0-35-generic"
}
```

## Datasources

By passing one or more datasource managers to Cohort, you can see at runtime the current state of the database pool(s).
Once enabled, a GET request to `/cohort/datasources` will return information such as idle connection count, pool size
and connection timeout levels.

Here is an example output for a Hikari datasource:

```json
[
  {
    "name": "HikariPool-2",
    "activeConnections": 0,
    "idleConnections": 8,
    "totalConnections": 8,
    "threadsAwaitingConnection": 0,
    "connectionTimeout": 30000,
    "idleTimeout": 600000,
    "maxLifetime": 1800000,
    "leakDetectionThreshold": 0,
    "maximumPoolSize": 16,
    "validationTimeout": 5000
  }
]
```

## System Properties

Send a GET request to `/cohort/sysprops` to return the current system properties.

Here is an example of the output:

```json
{
  "sun.jnu.encoding": "UTF-8",
  "java.class.path": "/home/sam/development/workspace/grindr/favorites2/favorites-app/build/classes/kotlin/main:/home/sam/development/workspace/grindr/favorites2/favorites-app/build/resources/main:/home/sam/development/workspace/grindr/favorites2/favorites-services/build/classes/kotlin/main:/home/sam/development/workspace/grindr/favorites2/favorites-datastore/build/classes/kotlin/main:/home/sam/development/workspace/grindr/favorites2/favorites-datastore/build/resources/main:/home/sam/.gradle/caches/modules-2/files-2.1/com.sksamuel.tabby/tabby-core-jvm/1.2.3/ac7f859f2c8969b621414dca9a583d48e5810b42/tabby-core-jvm-1.2.3.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-jdk8/1.6.0/baf82c475e9372c25407f3d132439e4aa803b8b8/kotlin-stdlib-jdk8-1.6.0.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.sksamuel.hoplite/hoplite-yaml/1.4.16/f3f7dbda38332aadf73362d3ce03ec4b5bedbd9b/hoplite-yaml-1.4.16.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.sksamuel.hoplite/hoplite-aws/1.4.16/cb99f7b54c41d7301c8fd3c2ce80480330fad653/hoplite-aws-1.4.16.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.sksamuel.hoplite/hoplite-core/1.4.16/87d565c4dda3cd26bfa36a836d87e5a4727d663e/hoplite-core-1.4.16.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.sksamuel.cohort/cohort-core/1.1.0.97-SNAPSHOT/fbbeb9ab99e673b6d6856d35cc02efa061bf8f8f/cohort-core-1.1.0.97-SNAPSHOT.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.sksamuel.cohort/cohort-ktor/1.1.0.97-SNAPSHOT/401322228c58d97692430c4730aacb4ac64b731b/cohort-ktor-1.1.0.97-SNAPSHOT.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.sksamuel.cohort/cohort-hikari/1.1.0.97-SNAPSHOT/6ec840b19613d005303a4b97d1226cf9a5a61701/cohort-hikari-1.1.0.97-SNAPSHOT.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.sksamuel.cohort/cohort-logback/1.1.0.97-SNAPSHOT/c8946b12b2c1f128653135c1baa0e68729975b34/cohort-logback-1.1.0.97-SNAPSHOT.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.micrometer/micrometer-registry-datadog/1.8.3/d6a3fd23932d374c430b4495c711fc86984d87c4/micrometer-registry-datadog-1.8.3.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api/1.7.36/6c62681a2f655b49963a5983b8b0950a6120ae14/slf4j-api-1.7.36.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-reflect/1.6.0/a215a7f914d5916dc5fd2d45cea16524e0220203/kotlin-reflect-1.6.0.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-server-netty/1.6.7/94a1c371efb5ce39ee4a2278034d2c86177c7e09/ktor-server-netty-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-auth/1.6.7/c3670ba12dd1e18fdbbf8de362deaf6edd9d0536/ktor-auth-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-jackson/1.6.7/12906204ff985a0894d57ed222ec6fbeb0eb27d2/ktor-jackson-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-metrics-micrometer/1.6.7/e4cfd8929dda03421536926e81d3bf1230876748/ktor-metrics-micrometer-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.zaxxer/HikariCP/5.0.1/a74c7f0a37046846e88d54f7cb6ea6d565c65f9c/HikariCP-5.0.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.springframework/spring-jdbc/5.3.15/2c7883bb9a50e37f177cdc3e064e88325f97bd36/spring-jdbc-5.3.15.jar:/home/sam/.gradle/caches/modules-2/files-2.1/mysql/mysql-connector-java/8.0.26/e5ec6610020a3084b7d32ee725d1650176f6b3de/mysql-connector-java-8.0.26.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.github.microutils/kotlin-logging-jvm/2.1.21/7a65a2789a27c67c281a4fe9e78689cf5e2b36d8/kotlin-logging-jvm-2.1.21.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-jdk7/1.6.0/da6bdc87391322974a43ccc00a25536ae74dad51/kotlin-stdlib-jdk7-1.6.0.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.amazonaws/aws-java-sdk-ssm/1.12.36/538689195096afbd3e69e91674144694e68f41bd/aws-java-sdk-ssm-1.12.36.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.amazonaws/aws-java-sdk-secretsmanager/1.12.36/1473e4d0ed9cf5f41bcb3c04672470e2504206c2/aws-java-sdk-secretsmanager-1.12.36.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.amazonaws/aws-java-sdk-core/1.12.36/841e201f238f834d52355580be16c08d137edee2/aws-java-sdk-core-1.12.36.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.micrometer/micrometer-core/1.8.3/d5f34a39442fb77773d0e4d814d1f6b1b8737b49/micrometer-core-1.8.3.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-codec-http2/4.1.69.Final/cfd4423c9d19683a6bf37e235830e73ec2f75ec1/netty-codec-http2-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.eclipse.jetty.alpn/alpn-api/1.1.3.v20160715/a1bf3a937f91b4c953acd13e8c9552347adc2198/alpn-api-1.1.3.v20160715.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-transport-native-kqueue/4.1.69.Final/b02f2b4051d17f4ec81daf7000231596ba82adf2/netty-transport-native-kqueue-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-transport-native-epoll/4.1.69.Final/22752c3332435a870157ca92583ac2566a60567e/netty-transport-native-epoll-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.googlecode.json-simple/json-simple/1.1.1/c9ad4a0850ab676c5c64461a05ca524cdfff59f1/json-simple-1.1.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.springframework/spring-tx/5.3.15/27b8c1f0a896dc2f7d1629556b06f55f2a07351d/spring-tx-5.3.15.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.springframework/spring-beans/5.3.15/a88e2ccfe8b131bcff2e643b90d52f6d928e7369/spring-beans-5.3.15.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.springframework/spring-core/5.3.15/e813c2311465672d3089fc7be8dbbadb04e64d6b/spring-core-5.3.15.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.google.protobuf/protobuf-java/3.11.4/7ec0925cc3aef0335bbc7d57edfd42b0f86f8267/protobuf-java-3.11.4.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.jetbrains/annotations/13.0/919f0dfe192fb4e063e7dacadee7f8bb9a2672a9/annotations-13.0.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.amazonaws/jmespath-java/1.12.36/b6b1e16d61591ec83777c2a7aecce3be75b8d87e/jmespath-java-1.12.36.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpclient/4.5.13/e5f6cae5ca7ecaac1ec2827a9e2d65ae2869cada/httpclient-4.5.13.jar:/home/sam/.gradle/caches/modules-2/files-2.1/commons-logging/commons-logging/1.2/4bfc12adfe4842bf07b657f0369c4cb522955686/commons-logging-1.2.jar:/home/sam/.gradle/caches/modules-2/files-2.1/commons-codec/commons-codec/1.15/49d94806b6e3dc933dacbd8acb0fdbab8ebd1e5d/commons-codec-1.15.jar:/home/sam/.gradle/caches/modules-2/files-2.1/software.amazon.ion/ion-java/1.0.2/ee9dacea7726e495f8352b81c12c23834ffbc564/ion-java-1.0.2.jar:/home/sam/.gradle/caches/modules-2/files-2.1/joda-time/joda-time/2.8.1/f5bfc718c95a7b1d3c371bb02a188a4df18361a9/joda-time-2.8.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.hdrhistogram/HdrHistogram/2.1.12/6eb7552156e0d517ae80cc2247be1427c8d90452/HdrHistogram-2.1.12.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-server-host-common/1.6.7/c0212569569df574e979bd0e88fb5b1a7747797e/ktor-server-host-common-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-codec-http/4.1.69.Final/702c6104a716ca9dc8eec6f4459733fdfcbe1959/netty-codec-http-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-handler/4.1.69.Final/73e932059ce8881094cedcfe4a92b8391ffa7b1b/netty-handler-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-codec/4.1.69.Final/617ecaee8bcb3874234e8c0832843ebbc9e9dcc9/netty-codec-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-transport/4.1.69.Final/228e5318012edb93138005ebe896b5dd91d4b47d/netty-transport-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-buffer/4.1.69.Final/c87da90e422b331ecd1e157ca77e5300348b6d0d/netty-buffer-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-common/4.1.69.Final/810e0649787c4930a8e699d68eb8519df2560dd0/netty-common-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-transport-native-unix-common/4.1.69.Final/b055314f117262c23df91a3874bebf0a53f3e24/netty-transport-native-unix-common-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-server-core/1.6.7/938a99fccb2cc0195ba97cfde1ec4eadbc210a5a/ktor-server-core-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-client-core-jvm/1.6.7/6732da42e91ee8d51abfc1d4113606956f1dd7/ktor-client-core-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.springframework/spring-jcl/5.3.15/88da960b4fcbd28621aea8b9911976adc06afce4/spring-jcl-5.3.15.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.apache.httpcomponents/httpcore/4.4.13/853b96d3afbb7bf8cc303fe27ee96836a10c1834/httpcore-4.4.13.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.netty/netty-resolver/4.1.69.Final/d4e7b1b8c1e5aeaf41877906b90762f1e0151902/netty-resolver-4.1.69.Final.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.typesafe/config/1.4.1/19058a07624a87f90d129af7cd9c68bee94535a9/config-1.4.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-http-cio-jvm/1.6.7/7fb3f35841398d7bcf95a8917c7e4ebb7bef61e3/ktor-http-cio-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-http-jvm/1.6.7/eb564a2a40e64d786d914a989759abd8c553e419/ktor-http-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-utils-jvm/1.6.7/b746be94f9ed4ceac132930e84a41059d9490d9f/ktor-utils-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-network-jvm/1.6.7/d59262677cab11f50be579a468e3bfe34efd0b80/ktor-network-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/io.ktor/ktor-io-jvm/1.6.7/375001bb92e11a788f93a58523ed73d9a0774e02/ktor-io-jvm-1.6.7.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-jdk8/1.6.0/49fdbe0570870f025f293c25f5e58fdf5be8dafb/kotlinx-coroutines-jdk8-1.6.0.jar:/home/sam/.gradle/caches/modules-2/files-2.1/ch.qos.logback/logback-classic/1.2.11/4741689214e9d1e8408b206506cbe76d1c6a7d60/logback-classic-1.2.11.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.datatype/jackson-datatype-jsr310/2.13.1/1ece5a87b59701328215e0083448b4d451857cbd/jackson-datatype-jsr310-2.13.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.module/jackson-module-kotlin/2.13.1/e8633bb172310b84462ebf586058b3babdbaf4b1/jackson-module-kotlin-2.13.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-common/1.6.10/c118700e3a33c8a0d9adc920e9dec0831171925/kotlin-stdlib-common-1.6.10.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-databind/2.13.1/698b2d2b15d9a1b7aae025f1d9f576842285e7f6/jackson-databind-2.13.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-core/2.13.1/51ae921a2ed1e06ca8876f12f32f265e83c0b2b8/jackson-core-2.13.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/1.6.10/b8af3fe6f1ca88526914929add63cf5e7c5049af/kotlin-stdlib-1.6.10.jar:/home/sam/.gradle/caches/modules-2/files-2.1/ch.qos.logback/logback-core/1.2.11/a01230df5ca5c34540cdaa3ad5efb012f1f1f792/logback-core-1.2.11.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/1.6.0/f3b8fd26c2e76d2f18cbc36aacb6e349fcb9fd5f/kotlinx-coroutines-core-jvm-1.6.0.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.yaml/snakeyaml/1.28/7cae037c3014350c923776548e71c9feb7a69259/snakeyaml-1.28.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-annotations/2.13.1/1cbcbe4623113e6af92ccaa89884a345270f1a87/jackson-annotations-2.13.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.fusesource.jansi/jansi/2.4.0/321c614f85f1dea6bb08c1817c60d53b7f3552fd/jansi-2.4.0.jar:/home/sam/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.dataformat/jackson-dataformat-cbor/2.13.1/35cc80045c638fe6a383d64c87f2d513502abffa/jackson-dataformat-cbor-2.13.1.jar:/home/sam/.gradle/caches/modules-2/files-2.1/org.latencyutils/LatencyUtils/2.0.3/769c0b82cb2421c8256300e907298a9410a2a3d3/LatencyUtils-2.0.3.jar",
  "java.vm.vendor": "AdoptOpenJDK",
  "java.vendor.url": "https://adoptopenjdk.net/",
  "user.timezone": "America/Chicago",
  "os.name": "Linux",
  "java.vm.specification.version": "11",
  "user.country": "US",
  "sun.boot.library.path": "/home/sam/.sdkman/candidates/java/11.0.10.hs-adpt/lib",
  "sun.java.command": "com.myapp.MainKt",
  "user.home": "/home/sam",
  "java.version.date": "2021-01-19",
  "java.home": "/home/sam/.sdkman/candidates/java/11.0.10.hs-adpt",
  "file.separator": "/",
  "java.vm.compressedOopsMode": "Zero based",
  "line.separator": "\n",
  "java.specification.name": "Java Platform API Specification",
  "java.vm.specification.vendor": "Oracle Corporation",
  "sun.management.compiler": "HotSpot 64-Bit Tiered Compilers",
  "java.runtime.version": "11.0.10+9",
  "user.name": "sam",
  "path.separator": ":",
  "file.encoding": "UTF-8",
  "java.vm.name": "OpenJDK 64-Bit Server VM",
  "user.dir": "/home/sam/development/workspace/grindr/favorites2",
  "os.arch": "amd64",
  "java.vm.specification.name": "Java Virtual Machine Specification",
  "java.awt.printerjob": "sun.print.PSPrinterJob",
  "java.class.version": "55.0"
}
```
