package co.egeek.corda.bug.gradle.plugin.child3.grand1.great1

import co.egeek.corda.bug.gradle.plugin.child1.grand1.ICanLog
import mu.KotlinLogging
import org.apache.commons.beanutils.BeanUtils

class Child1 :
    ICanLog {

    override fun logMessage() {
        log.debug { "Child1" }
        BeanUtils.describe(String())
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}