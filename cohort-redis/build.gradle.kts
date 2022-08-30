dependencies {
   implementation(projects.cohortCore)
   api("redis.clients:jedis:_")
}

apply("../publish.gradle.kts")
