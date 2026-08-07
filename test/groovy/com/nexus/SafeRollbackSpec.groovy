#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class SafeRollbackSpec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Înregistrăm metoda mock pentru comanda shell (unde rulează rollback-ul)
        helper.registerAllowedMethod('sh', [String.class], null)
    }

    def "test safeRollback executes critical block successfully when no error occurs"() {
        setup:
        def safeRollback = loadScript('vars/safeRollback.groovy')
        def executed = false

        when:
        safeRollback([releaseName: 'my-app']) {
            executed = true
        }

        then:
        assert executed == true
        assert !helper.callStack.any { it.methodName == 'sh' }
    }

    def "test safeRollback triggers rollback command when critical block throws exception"() {
        setup:
        def safeRollback = loadScript('vars/safeRollback.groovy')

        when:
        safeRollback([releaseName: 'my-app', namespace: 'prod']) {
            throw new RuntimeException("Deployment exploded!")
        }

        then:
        thrown(Exception)
        assert helper.callStack.any { call ->
            call.methodName == 'sh' && call.args[0].contains('helm rollback my-app --namespace prod')
        }
    }
}
