# Changelog

### 1.4.0

* Added shutdown hooks
  * `EngineShutdownHook` used for graceful shutdown of the ktor server
* Added `endpointsPrefix` setting to config, to allow adjusting the namespace under which endpoints are registered
* Added `autoEndpoints` option to allow installing the endpoints inside your own routing space - in order to support authentication, or other around advice.
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
