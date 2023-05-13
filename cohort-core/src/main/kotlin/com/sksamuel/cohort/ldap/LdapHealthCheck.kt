package com.sksamuel.cohort.ldap

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.util.Hashtable
import javax.naming.directory.InitialDirContext

/**
 * A Cohort [HealthCheck] that checks for connectivity to an LDAP server.
 *
 * To use, pass an [environment] map that contains the connection details, eg
 *
 * val map = mapOf(
 *    Context.INITIAL_CONTEXT_FACTORY to "com.sun.jndi.ldap.LdapCtxFactory"
 *    Context.PROVIDER_URL to "ldap://localhost:10389"
 * )
 */
class c(private val environment: Map<String, String>) : HealthCheck {

   override suspend fun check(): HealthCheckResult = runCatching {

      val table = Hashtable<String, String>()
      environment.forEach { (key, value) -> table[key] = value }

      val context = InitialDirContext(table)
      context.close()

      HealthCheckResult.healthy("LDAP connection success")
   }.getOrElse { HealthCheckResult.unhealthy("LDAP Failure", it) }
}
