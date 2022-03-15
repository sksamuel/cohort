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

## Logging

Cohort allows you to view the current logging configuration and update log levels at runtime.

## Runtime Information

Cohort provides several endpoints to reflect the current environment.

* operating system - displays the running os and version
