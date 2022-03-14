package com.sksamuel.cohort.os

import java.lang.management.ManagementFactory

fun getOperatingSystem(): OperatingSystem = OperatingSystem(
  ManagementFactory.getOperatingSystemMXBean().arch,
  ManagementFactory.getOperatingSystemMXBean().name,
  ManagementFactory.getOperatingSystemMXBean().version,
)

data class OperatingSystem(
  val arch: String,
  val name: String,
  val version: String,
)
