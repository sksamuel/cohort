# Changelog

### 2.2.1

* Added `logHealthyStatus` option to logging subscribers

### 2.2.0

* Added new Kafka healthchecks
* Renamed `cohort-redis` module to `cohort-jedis`
* Added `Log4j2HealthCheckLogging` and `LogbackHealthCheckLogging` subscribers to log results of healthchecks

### 2.1.3

* Added `S3WriteBucketHealthCheck`

### 2.1.2

* Added `ArrayCopyWarmup`
* Added `EndpointStartupHealthCheck`
* Added `HttpRequestWarmup`

### 2.1.1

* Allow specifying the coroutine dispatcher when creating a `KafkaConsumerSubscriptionHealthCheck`.
* Fixed incorrect message on the `ThreadStateHealthCheck`.

### 2.1.0

* Introduced new `WarmupRegistry` to allow warmups to specify duration instead of iteration counts.
* Allow routes to be registered in any Ktor routing block
* Added `KafkaConsumerSubscriptionHealthCheck`
* Added `close` method to `HealthCheckRegistry`
* Auto shutdown `HealthCheckRegistry` when JVM is terminating.
* Added `FibonacciWarmup`, `DataSourceConnectionWarmup`, `RedisClusterConnectionWarmup`, `JacksonMapperWarmup`.

### 2.0.2

* Fixed race condition with 2.0.x warmup starts

### 2.0.1

* Fixed micrometer metrics tag name for status tag
* Fixed Cohort plugin package name
* Added overloaded registry method

### 2.0.0

**Minimum version of Kotlin is now 1.8**

* Removed Ktor 1.x module, and merged Ktor 2.x module into the `cohort-core` module.
  * No additional dependencies are required for ktor.
* Added `HttpWarmup`, `RedisConnectionWarmup`, `FibWarmup`, `JacksonWarmup`, `DataSourceWarmup`
  warm up health checks which will
  execute for a period of time to warm the JVM / connections before reporting as healthy. These are intended to be used
  by startup probes in K8.
* Added `HotSpotCompilationTimeHealthCheck` which will report healthy once Hotspot has performed a given amount of
  compilation.
* Added `cohort-lettuce` module which contains Redis health and warmup checks using the Lettuce client, in contrast to
  the `cohort-redis` module which uses the Jedis client.
* Added `PulsarHealthCheck` for Apache Pulsar clusters.
* Added `DynamoDBHealthCheck` for AWS Dynamo DB instances.
* Changed TcpHealthCheck package name

### 1.7.3

* Fix logic with `errorOnYellow` in elastic cluster checks.

### 1.7.2

* Fixed S3 and SQS checks to close client correctly.

### 1.7.1

* Added `DatabaseConnectionHealthCheck` as a long term replacement for `DatabaseHealthCheck` (which is now deprecated)
  for checking that a `DataSource` can provide a connection and that the connection is valid. This variant uses the
  JDBC4 `isValid` method rather than executing an arbitrary query.

### 1.7.0

* Added `RabbitConnectionHealthCheck` which checks for connectivity to a RabbitMQ instance.
* Added `MongoConnectionHealthCheck` which checks for connectivity to a MongoDB instance.
* Added `SNSHealthCheck` which checks for connectivity to an AWS SNS instance.
* Added `ElasticIndexHealthCheck` which checks for presence of a topic, with option to fail if the topic is empty.
* Allow custom tags to be set on Cohort micrometer integration.

### 1.6.2

* Added better automatically generated health check names

### 1.6.1

* Removed `com.sksamuel` prefix from automatically generated health check names when sending to micrometer.
* Fixed calculation bug in `GarbageCollectionTimeCheck`

### 1.6.0

* Added `cohort-micrometer` module for sending healthcheck metrics to a micrometer registry.

### 1.5.0

* Added AWS `S3ReadBucketHealthCheck`
* Added AWS `SQSQueueHealthCheck`
* Deprecated Shutdown hooks in favour of built-in Ktor `onShutdown`.
* Added arbitrary command support to redis health checks.

### 1.4.1

* Fixed dbcp package names.

### 1.4.0

* Added shutdown hooks
* `EngineShutdownHook` used for graceful shutdown of the ktor server
* Added `endpointsPrefix` setting to config, to allow adjusting the namespace under which endpoints are registered
* Added `autoEndpoints` option to allow installing the endpoints inside your own routing space - in order to support
  authentication, or other around advice.
* Added eviction endpoint for datasources to evict idle connections

### 1.3.0

* Added new Kafka healthchecks:
   * `KafkaConsumerRecordsConsumedRateHealthCheck`
   * `KafkaProducerRecordSendRateHealthCheck`
   * `KafkaConsumerLastPollTimeHealthCheck`
* Added new `/cohort/memory` endpoint for reporting memory and buffer pool information
* Added new database min idle connection healthchecks:
   * `HikariMinIdleHealthCheck`
   * `DbcpMinIdleHealthCheck`
* Added `MaxFileDescriptorsHealthCheck`
* Added `ProcessCpuHealthCheck`

### 1.2.2

* Updated Kakfa health checks to use `AdminClient` instead of props.

### 1.2.1

* Updated Elastic health checks to accept `RestHighLevelClient` instead of hostnames.
*

### 1.2.0

* Added support for Ktor2
* Added invariant that healthcheck register is not empty

### 1.1.7

* Added `logUnhealthy` option (default true) to log unhealthy checks
* Added `startUnhealthy` option (default true) to HealthCheckRegistry that starts up all checks in failed mode

### 1.1.6

* Added `HttpHealthCheck` for testing connectivity to http endpoints.
* Tweaked configuration settings for redis health checks

### 1.1.5

* Allow the Kafka client to be customized by passing in properties.
* Added healthcheck override that accepts a function `() -> Result<String>`

### 1.1.4

* Added `/cohort/gc` endpoint

### 1.1.3

* First publicised release.
