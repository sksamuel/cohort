package com.sksamuel.cohort.memory

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory
import kotlin.math.roundToInt
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A Cohort [HealthCheck] that checks for time spent in GC as a percentage between invocations.
 *
 * A value of 0 would indicate zero GC time and a value of 100 would indicate all time was spent in GC.
 *
 * The check is considered healthy if the amount of time in GC is <= [maxGcTime].
 */
@kotlin.time.ExperimentalTime
class GarbageCollectionTimeCheck(private val maxGcTime: Int) : HealthCheck {

  private val beans = ManagementFactory.getGarbageCollectorMXBeans()
  private var lastTime = 0L
  private val source = TimeSource.Monotonic
  private var lastMark: TimeMark? = null

  override val name: String = "garbage_collection_time"

  override suspend fun check(): HealthCheckResult {

    // count all the time spent gc'ing since startup
    val cumulativeTime = beans.map { it.collectionTime }.filter { it >= 0 }.sum()
    val timeDiff = cumulativeTime - lastTime
    lastTime = cumulativeTime

    val elapsed = lastMark?.elapsedNow()?.inWholeMilliseconds
    lastMark = source.markNow()

    return if (elapsed == null) {
      HealthCheckResult.healthy("GC Collection time was 0%")
    } else {
      val pc = (timeDiff / elapsed.toDouble()).roundToInt()
      val message = "GC Collection time was ${timeDiff}ms / $pc% [Max is $maxGcTime%]"
      if (pc <= maxGcTime) {
        HealthCheckResult.healthy(message)
      } else {
        HealthCheckResult.unhealthy(message, null)
      }
    }
  }
}
