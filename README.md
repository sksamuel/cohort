# Cohort

![main](https://github.com/sksamuel/cohort/workflows/main/badge.svg)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.cohort/cohort-core.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Ccohort)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.cohort/cohort-core.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/cohort/)

Cohort is a [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) style
replacement for non Spring services. It provides HTTP endpoints to help monitor and manage apps in production. For
example health, logging, database and JVM metrics.

## How to use

Include the core dependency `com.sksamuel.cohort:cohort-core:<version>` in your build along with the additional modules
for the features you wish to activate.

## Healthchecks

Cohort provides health checks for a variety of JVM metrics such as memory and thread deadlocks as well as connectivity
to services such as Kafka and Elasticsearch and databases.

## Logging

Cohort allows you to view the current logging configuration and update log levels at runtime.

## Runtime Information

Cohort provides several endpoints to reflect the current environment.

* operating system - displays the running os and version
