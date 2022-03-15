package com.sksamuel.cohort.jvm

import java.lang.management.ManagementFactory

fun getJvmDetails(): Result<Jvm> = runCatching {
  val bean = ManagementFactory.getRuntimeMXBean()
  Jvm(
    name = bean.name,
    pid = bean.pid,
    vmOptions = bean.inputArguments,
    bootClassPath = bean.bootClassPath,
    classPath = bean.classPath,
    specName = bean.specName,
    specVendor = bean.specVendor,
    specVersion = bean.specVersion,
    vmName = bean.vmName,
    vmVendor = bean.vmVendor,
    vmVersion = bean.vmVersion,
    startTime = bean.startTime,
    uptime = bean.uptime,
  )
}

data class Jvm(
  val name: String,
  val pid: Long,
  val vmOptions: List<String>,
  val bootClassPath: String,
  val classPath: String,
  val specName: String,
  val specVendor: String,
  val specVersion: String,
  val vmName: String,
  val vmVendor: String,
  val vmVersion: String,
  val startTime: Long,
  val uptime: Long
)
