dependencies {
   implementation(project(":cohort-core"))
   implementation("org.apache.kafka:kafka-clients:3.1.0")
   implementation("redis.clients:jedis:4.1.1")
}

apply("../publish.gradle.kts")
