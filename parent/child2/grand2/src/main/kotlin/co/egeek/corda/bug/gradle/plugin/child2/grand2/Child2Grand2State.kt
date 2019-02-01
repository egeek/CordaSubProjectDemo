package co.egeek.corda.bug.gradle.plugin.child2.grand2

import co.egeek.corda.bug.gradle.plugin.child1.grand1.Child1Grand1
import co.egeek.corda.bug.gradle.plugin.child1.grand1.ICanLog
import co.egeek.corda.bug.gradle.plugin.child1.grand2.Child1Grand2
import mu.KotlinLogging
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import org.apache.commons.beanutils.BeanUtils

open class Child2Grand2State(override val linearId: UniqueIdentifier, override val participants: List<AbstractParty>) :
    LinearState,
    Child1Grand1,
    Child1Grand2,
    ICanLog {

    override fun logMessage() {
        log.debug { "Child2Grand2State" }
        BeanUtils.describe(String())
    }

    override fun logMessageAgain() {
        log.debug { "Child2Grand2State again" }
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}