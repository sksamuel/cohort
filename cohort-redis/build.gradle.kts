dependencies {
   implementation(projects.cohortCore)
   implementation("redis.clients:jedis:_")
}

apply("../publish.gradle.kts")
