package com.sksamuel.cohort.memory

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory
import kotlin.math.roundToInt
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A Cohort [HealthCheck] that checks for time spent in GC as a percentage of total time in the lookback period.
 *
 * A value of 0 would indicate zero GC time and a value of 100 would indicate all time was spent in GC.
 *
 * The check is considered healthy if the amount of time in GC is <= [maxGcTime].
 */
class GarbageCollectionTimeCheck(private val maxGcTime: Int) : HealthCheck {

  private val beans = ManagementFactory.getGarbageCollectorMXBeans()
  private var lastTime = 0L
  private val source = TimeSource.Monotonic
  private var lastMark: TimeMark? = null

  override suspend fun check(): HealthCheckResult {
    val time = beans.map { it.collectionTime }.filter { it >= 0 }.sum()
    lastMark?.let {
      val diff = time - lastTime
      val period = it.elapsedNow().inWholeMilliseconds
      val pc = (diff / period.toDouble()).roundToInt()
      return if (pc <= maxGcTime) {
        HealthCheckResult.Healthy("GC Collection time was $pc% [Max is $maxGcTime]")
      } else {
        HealthCheckResult.Unhealthy("GC Collection time was $pc% [Max is $maxGcTime]", null)
      }
    }
    lastMark = source.markNow()
    lastTime = time
    return HealthCheckResult.Healthy("GC Collection time was 0%")
  }
}
