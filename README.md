# Cohort

![main](https://github.com/sksamuel/cohort/workflows/main/badge.svg)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.cohort/cohort-core.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Ccohort)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.cohort/cohort-core.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/cohort/)

Cohort is a [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) style
replacement for [Ktor](https://ktor.io). It provides HTTP endpoints to help monitor and manage apps in production. For
example healthchecks, logging, database and JVM metrics.

See [changelog](changelog.md)

## Features

**All features are disabled by default**.

* **Comprehensive system healthchecks:** Expose healthcheck endpoints that check for thread deadlocks, memory usage,
  disk space, cpu usage, garbage collection and more.
* **Resource healthchecks:** Additional modules to monitor the health of Redis, Kafka, Elasticsearch, databases and
  other resources.
* **Database pools:** See runtime metrics such as active and idle connections in database pools such as Hikari
  Connection Pool.
* **JVM Info:** Enable endpoints to export system properties, JVM arguments and version information, and O/S name / version.
* **Thread and heap dumps:** Optional endpoints to export a thread dump or heap dump, in the standard JVM format, for
  analysis locally.
* **Database migrations:** See the status of applied and pending database migrations from
  either [Flyway](https://flywaydb.org/) or [Liquibase](https://liquibase.org/).
* **Logging configuration:** View configured loggers and levels and modify log levels at runtime.

## How to use

Include the following dependencies in your build:

* `com.sksamuel.cohort:cohort-core:<version>`
* `com.sksamuel.cohort:cohort-ktor:<version>`

along with the additional modules for any features you wish to activate. For example the kafka module
requires `com.sksamuel.cohort:cohort-kafka:<version>`.

Then to wire into Ktor, install the `Cohort` plugin, and enable whichever features / endpoints we want to expose.

Here is a sample configuration with each feature enabled.

```kotlin
install(Cohort) {

  // enable an endpoint to display operating system name and version
  operatingSystem = true

  // enable runtime JVM information such as vm options and vendor name
  jvmInfo = true

  // configure the Logback log manager to show effective log levels and allow runtime adjustment
  logManager = LogbackManager

  // show connection pool information
  dataSources = listOf(HikariDataSourceManager(ds))

  // show current system properties
  sysprops = true

  // enable an endpoint to dump the heap in hprof format
  heapdump = true

  // enable an endpoint to dump threads
  threaddump = true

  // enable healthchecks for kubernetes
  // each of these is optional and can map to any healthchecks/url you wish
  healthcheck("/liveness", livechecks)
  healthcheck("/readiness", readychecks)
  healthcheck("/startup", startupchecks)
}
```

## Healthchecks

Cohort provides `HealthCheck`s for a variety of JVM metrics such as memory and thread deadlocks as well as connectivity
to services such as Kafka and Elasticsearch and databases.

We use health checks by adding them to a `HealthCheckRegistry` instance, along with an interval of how often to run the
checks. A registry requires a _coroutine dispatcher_ to execute the checks on. Healthchecks can take advantage of
coroutines to suspend if they need to do something IO based. Cohort will periodically run these healthchecks based on
the passed schedule and record if they are healthy or unhealthy.

For example:

```kotlin
val checks = HealthCheckRegistry(Dispatchers.Default) {

  // detects if threads are mutually blocked on each others locks
  register(ThreadDeadlockHealthCheck(), 1.minutes)

// we should never have zero database connections
  register("reader connections", HikariConnectionsHealthCheck(ds, 1), 5.seconds)
}
```

With the registry created, we register it with Cohort by invoking the `healthcheck` method along with an endpoint url to
expose it on.

For example:

```kotlin
install(Cohort) {
  healthcheck("/healthcheck", checks)
}
```

Whenever the endpoint is accessed, a `200` is returned if all health checks are currently reporting healthy, and a `500`
otherwise.

Which healthchecks you use is entirely up to you, and you may want to use some healthchecks for startup probes, some for
readiness checks and some for liveness checks. See the section on [kubernetes](#Kubernetes) for discussion on how to
structure healthchecks in a kubernetes environment.

### Available Healthchecks

This table lists the available health checks and their uses.

| Healthcheck                     | Details                                                                                                                                                                                                                                          |
|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AvailableCoresHealthCheck       | Checks for a minimum number of available CPU cores. While the number of cores won't change during the lifetime of a pod, this check can be useful to avoid accidentally deploying pods into environments that don't have the required resources. |
| DaemonThreadsHealthCheck        | Checks that the number of daemon threads does not exceed a threshold.                                                                                                                                                                            |
| DatabaseHealthCheck             | Checks that a database connection can be opened and a query executed                                                                                                                                                                             |
| DbcpConnectionsHealthCheck      | Checks that the number of connections in Apache DBCP2 connection pool is at least equal to a min value.                                                                                                                                          |
| DiskSpaceHealthCheck            | Checks that the available disk space on a filestore is below a threshold.                                                                                                                                                                        |
| ElasticClusterHealthCheck       | Checks that an elasticsearch cluster is reachable and in a healthy state.                                                                                                                                                                        |
| FreememHealthCheck              | Checks that the available freemem is above a threshold.                                                                                                                                                                                          |
| GarbageCollectionTimeCheck      | Checks that the time spent in GC is below a threshold. The time is specified as a percentage and is calculated as the period between invocations.                                                                                                |
| HikariConnectionsHealthCheck    | Confirms that the number of connections in a Hikari DataSource pool is equal or above a threshold. This is useful to ensure a required number of connections are open before accepting traffic.                                                  |
| HikariPendingThreadsHealthCheck | Checks that the number of threads awaiting a connection from a Hikari DataSource is below a threshold. This is useful to detect when queries are running slowly and causing threads to back up waiting for a connection                          |
| KafkaClusterHealthCheck         | Confirms that a Kafka client can connect to a Kafka cluster.                                                                                                                                                                                     |
| LiveThreadsHealthCheck          | Checks that the number of live threads does not exceed a value                                                                                                                                                                                   |
| LoadedClassesHealthCheck        | Checks that the number of loaded classes is below a threshold                                                                                                                                                                                    |
| OpenFileDescriptorsHealthCheck  | Checks that the number of open file descriptors is below a threshold.                                                                                                                                                                            |
| PeakThreadsHealthCheck          | Checks that the number of peak threads does not exceed a threshold.                                                                                                                                                                              |
| RedisClusterHealthCheck         | Confirms that a connection can be opened to a Redis cluster.                                                                                                                                                                                     |
| RedisHealthCheck                | Confirms that a connection can be opened to a Redis instance.                                                                                                                                                                                    |
| StartedThreadsHealthCheck       | that the number of created and started threads does not exceed a threshold.                                                                                                                                                                      |
| SystemCpuHealthCheck            | Checks that the maximum system cpu is below a threshold.                                                                                                                                                                                         |
| SystemLoadHealthCheck           | Checks that the maximum system load is below a threshold.                                                                                                                                                                                        |
| TcpHealthCheck                  | Attempts to ping a given host and port within a time period. Can be used to check connectivity to an arbitrary socket.                                                                                                                           |
| ThreadDeadlockHealthCheck       | Checks for the presence of deadlocked threads. A single deadlocked thread marks this check as unhealthy.                                                                                                                                         |
| ThreadStateHealthCheck          | Checks that the the number of threads in a given state does not exceed a value. For example, you could specify that the max number of BLOCKED threads is 100.                                                                                    |

### Kubernetes

A Kubernetes kubelet offers three kinds of probes to know the status of a container.

* _liveness_ - Indicates whether the container is running. If the liveness probe fails, the kubelet kills the
  container (and restarts subject to the restart policy).
* _readiness_ - Indicates whether the container is ready to respond to requests. If the readiness probe fails, the
  kubelet removes the pod from receiving traffic.
* _startup_ - Indicates whether the application within the container has started. All other probes are disabled if a
  startup probe is provided, until it succeeds.

The kubelet uses liveness probes to know when to restart a container. Liveness probes help catch a situation where an
application is running but is no longer useful. One such example is if a thread has stopped and the application does not
have code to detect and restart the thread. Restarting a container in such a state can make the application available
again despite the presence of bugs.

The kubelet uses readiness probes to know when a container should receive traffic. A pod is considered ready when all of
its containers are ready. One use of this signal is to temporarily remove traffic from backends when they are unable to
handle any more requests. For example, a service may have received more requests than it can handle, and so it's backlog
of requests is growing. Taking that pod out of the load balancers while it catches up can avoid the service crashing or
needing a restart. A pod with containers reporting that they are not ready does not receive traffic through Kubernetes
Services.

Readiness probes are not a substitute for proper scaling (either HPA or manually) but they can avoid a situation where
all pods are killed, and a service is completely unavailable.

The kubelet uses startup probes to know when a container application has fully started. If such a probe is configured,
it disables liveness and readiness checks until it succeeds, making sure those probes don't interfere with the
application startup. Startup probes are very useful if an application needs to perform slow initialization work and
until that is complete, a liveness check would fail. This avoids situation where the failing liveness checks result in
the kubelet killing the pod before it is ready.

### Healthcheck Endpoint Output

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

To enable this, pass an instance of the `LogManager` interface for the logging framework you are using to
the `logManager`parameter in the Cohort plugin configuration.

Once enabled, the endpoint `GET /cohort/logging` can be used to show current log information
and `PUT /cohort/logging/{name}/{level}` can be used to modify a log level at runtime.

Cohort currently supports two `LogManager` implementations:

* `LogbackManager` - add module `com.sksamuel.cohort:cohort-logback:<version>`
* `Log4j2Manager` - add module `com.sksamuel.cohort:cohort-log4j2:<version>`

For example, for projects that use logback, you can configure like this:

```kotlin
install(Cohort) {
  logManager = LogbackManager
}
```

Here is the example output of which shows the logging configuration:

```json
{
  "levels": [
    "DEBUG",
    "TRACE",
    "INFO",
    "ERROR",
    "OFF",
    "WARN"
  ],
  "loggers": [
    {
      "name": "ROOT",
      "level": "INFO"
    },
    {
      "name": "com",
      "level": "INFO"
    },
    {
      "name": "com.sksamuel",
      "level": "INFO"
    },
    {
      "name": "ktor",
      "level": "INFO"
    },
    {
      "name": "ktor.application",
      "level": "INFO"
    },
    {
      "name": "org",
      "level": "INFO"
    },
    {
      "name": "org.apache",
      "level": "INFO"
    },
    {
      "name": "org.apache.kafka",
      "level": "WARN"
    }
  ]
}
```

## Jvm Info

Displays information about the JVM state, including VM options, JVM version, and vendor name.

To enable, set `jvmInfo` to true inside the `Cohort` ktor configuration block:

```kotlin
install(Cohort) {
  jvmInfo = true
}
```

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

To enable, set `operatingSystem` to true inside the `Cohort` ktor configuration block:

```kotlin
install(Cohort) {
  operatingSystem = true
}
```

```json
{
  "arch": "amd64",
  "name": "Linux",
  "version": "5.13.0-35-generic"
}
```

## Datasources

By passing one or more database pools to Cohort, you can see at runtime the current state of the pool(s). Once enabled,
a GET request to `/cohort/datasources` will return information such as idle connection count, max pool size, connection
timeouts and so on.

Cohort supports two connection pool libraries:

* Apache Commons DBCP - add module `com.sksamuel.cohort:cohort-dbcp`
* HikariCP - add module `com.sksamuel.cohort:cohort-hikari`

To activate this feature, wrap your `DataSource` in an appropriate _DataSourceManager_ instance and pass through to the Cohort plugin.

For example, if we had two connection pools, a writer pool using Hikari, and a reader pool using Apache DBCP, then we could configure like this:

```kotlin
install(Cohort) {
  dataSources = listOf(
    ApacheDBCPDataSourceManager(reader),
    HikariDataSourceManager(writer),
  )
}
```

Here is an example output for the above datasources:

```json
[
  {
    "name": "writer",
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
  },
  {
    "name": "reader",
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

To enable, set `sysprops` to true inside the `Cohort` plugin configuration block:

```kotlin
install(Cohort) {
  sysprops = true
}
```

Here is an example of the output:

```json
{
  "sun.jnu.encoding": "UTF-8",
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
  "user.dir": "/home/sam/development/workspace/myapp",
  "os.arch": "amd64",
  "java.vm.specification.name": "Java Virtual Machine Specification",
  "java.awt.printerjob": "sun.print.PSPrinterJob",
  "java.class.version": "55.0"
}
```
## Heap Dump

Send a GET request to `/cohort/heapdump` to retrieve a heap dump for all live objects.

The file returned is in the format used by [hprof](https://docs.oracle.com/javase/7/docs/technotes/samples/hprof.html).

To enable, set `heapdump` to true inside the `Cohort` plugin configuration block:

```kotlin
install(Cohort) {
  heapdump = true
}
```

## Thread Dump

Send a GET request to `/cohort/threaddump` to retrieve a thread dump for all current threads.

To enable, set `threaddump` to true inside the `Cohort` plugin configuration block:

```kotlin
install(Cohort) {
  threaddump = true
}
```

Example output:

```text
"main" prio=5 Id=1 WAITING on io.netty.channel.AbstractChannel$CloseFuture@291c536c
	at java.base@11.0.10/java.lang.Object.wait(Native Method)
	-  waiting on io.netty.channel.AbstractChannel$CloseFuture@291c536c
	at java.base@11.0.10/java.lang.Object.wait(Object.java:328)
	at app//io.netty.util.concurrent.DefaultPromise.await(DefaultPromise.java:253)
	at app//io.netty.channel.DefaultChannelPromise.await(DefaultChannelPromise.java:131)
	at app//io.netty.channel.DefaultChannelPromise.await(DefaultChannelPromise.java:30)
	at app//io.netty.util.concurrent.DefaultPromise.sync(DefaultPromise.java:404)
	at app//io.netty.channel.DefaultChannelPromise.sync(DefaultChannelPromise.java:119)
	at app//io.netty.channel.DefaultChannelPromise.sync(DefaultChannelPromise.java:30)
	...

"Reference Handler" daemon prio=10 Id=2 RUNNABLE
	at java.base@11.0.10/java.lang.ref.Reference.waitForReferencePendingList(Native Method)
	at java.base@11.0.10/java.lang.ref.Reference.processPendingReferences(Reference.java:241)
	at java.base@11.0.10/java.lang.ref.Reference$ReferenceHandler.run(Reference.java:213)

"Finalizer" daemon prio=8 Id=3 WAITING on java.lang.ref.ReferenceQueue$Lock@1392e5c7
	at java.base@11.0.10/java.lang.Object.wait(Native Method)
	-  waiting on java.lang.ref.ReferenceQueue$Lock@1392e5c7
	at java.base@11.0.10/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
	at java.base@11.0.10/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:176)
	at java.base@11.0.10/java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:170)

"Signal Dispatcher" daemon prio=9 Id=4 RUNNABLE

"Common-Cleaner" daemon prio=8 Id=19 TIMED_WAITING on java.lang.ref.ReferenceQueue$Lock@78e1959d
	at java.base@11.0.10/java.lang.Object.wait(Native Method)
	-  waiting on java.lang.ref.ReferenceQueue$Lock@78e1959d
	at java.base@11.0.10/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
	at java.base@11.0.10/jdk.internal.ref.CleanerImpl.run(CleanerImpl.java:148)
	at java.base@11.0.10/java.lang.Thread.run(Thread.java:834)
	at java.base@11.0.10/jdk.internal.misc.InnocuousThread.run(InnocuousThread.java:134)
```
