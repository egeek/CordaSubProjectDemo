package co.egeek.corda.bug.gradle.plugin.child3.grand1.great2

import co.egeek.corda.bug.gradle.plugin.child1.grand2.ICanLogAgain
import mu.KotlinLogging
import org.apache.commons.beanutils.BeanUtils

class Child3Grand1Great2 :
    ICanLogAgain {

    override fun logMessageAgain() {
        log.debug { "Child1" }
        BeanUtils.describe(String())
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}