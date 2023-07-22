package com.sksamuel.cohort.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder

internal object Counter {

   private val adders = ConcurrentHashMap<String, LongAdder>()

   fun count(consumerGroupId: String): Long {
      return adders.getOrPut(consumerGroupId) { LongAdder() }.toLong()
   }

   fun counts(): Map<String, Long> {
      return adders.mapValues { it.value.toLong() }
   }

   fun add(consumerGroupId: String, n: Long) {
      adders.getOrPut(consumerGroupId) { LongAdder() }.add(n)
   }

   fun clear(consumerGroupId: String) {
      adders.remove(consumerGroupId)
   }
}

class CountingConsumerInterceptor : ConsumerInterceptor<Any, Any> {

   private var groupId: String? = null

   override fun close() {
      groupId?.let { Counter.clear(it) }
   }

   override fun configure(configs: MutableMap<String, *>) {
      groupId = configs[ConsumerConfig.GROUP_ID_CONFIG]?.toString() ?: error("GROUP_ID_CONFIG must be specified")
   }

   override fun onCommit(offsets: MutableMap<TopicPartition, OffsetAndMetadata>?) {}

   override fun onConsume(records: ConsumerRecords<Any, Any>): ConsumerRecords<Any, Any> {
      Counter.add(groupId ?: error("GROUP_ID_CONFIG must be specified"), records.toList().size.toLong())
      return records
   }
}
