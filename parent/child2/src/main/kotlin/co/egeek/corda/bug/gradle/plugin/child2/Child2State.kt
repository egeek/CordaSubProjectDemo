package co.egeek.corda.bug.gradle.plugin.child2

import mu.KotlinLogging
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import org.apache.commons.beanutils.BeanUtils
import co.egeek.corda.bug.gradle.plugin.child1.Child1
import co.egeek.corda.bug.gradle.plugin.child1.ICanLog

open class Child2State(override val linearId: UniqueIdentifier, override val participants: List<AbstractParty>) :
    LinearState,
    Child1,
    ICanLog {

    override fun logMessage() {
        log.debug { "Child2State" }
        BeanUtils.describe(String())
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}