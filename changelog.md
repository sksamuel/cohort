# Changelog

### 1.1.6

* Added `HttpHealthCheck` for testing connectivity to http endpoints.
* Added `startUnhealthy` option (default true) to HealthCheckRegistry that starts up all checks in failed mode
* Tweaked configuration settings for redis health checks

### 1.1.5

* Allow the Kafka client to be customized by passing in properties.
* Added healthcheck override that accepts a function `() -> Result<String>`

### 1.1.4

* Added `/cohort/gc` endpoint

### 1.1.3

* First publicised release.
