package co.egeek.corda.bug.gradle.plugin.child1.grand2

import mu.KotlinLogging
import net.corda.core.serialization.CordaSerializable
import org.apache.commons.beanutils.BeanUtils

@CordaSerializable
open class Child1Grand2 :
    ICanLogAgain {

    override fun logMessageAgain() {
        log.debug { "Child1Grand2" }
        BeanUtils.describe(String())
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}