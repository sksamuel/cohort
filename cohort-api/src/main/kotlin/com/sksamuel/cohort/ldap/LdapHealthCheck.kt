package com.sksamuel.cohort.ldap

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
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
class LdapHealthCheck(
   private val environment: Map<String, String>,
   override val name: String = "ldap",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult = runCatching {

      val table = Hashtable<String, String>()
      environment.forEach { (key, value) -> table[key] = value }

      runInterruptible(Dispatchers.IO) {
         // InitialDirContext's constructor opens an LDAP/TCP connection. If anything happens
         // between construction and the bare close() call (an exception, an interrupt fired by
         // a parent cancellation, future code injected in between) the context would leak its
         // socket. try/finally ensures close runs in all paths.
         val context = InitialDirContext(table)
         try {
            // No-op: construction itself is the liveness probe.
         } finally {
            context.close()
         }
      }

      HealthCheckResult.healthy("LDAP connection success")
   }.getOrElse { HealthCheckResult.unhealthy("LDAP Failure", it) }
}
