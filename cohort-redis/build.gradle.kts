dependencies {
   implementation(project(":cohort-core"))
   implementation("org.apache.kafka:kafka-clients:_")
   implementation("redis.clients:jedis:_")
}

apply("../publish.gradle.kts")
