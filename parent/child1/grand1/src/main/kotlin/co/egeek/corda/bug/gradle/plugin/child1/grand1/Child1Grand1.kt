package co.egeek.corda.bug.gradle.plugin.child1.grand1

import mu.KotlinLogging
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.beanutils.BeanUtils

@CordaSerializable
open class Child1Grand1 :
    ICanLog {

    override fun logMessage() {
        log.debug { "Child1Grand1" }
        BeanUtils.describe(String())
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}