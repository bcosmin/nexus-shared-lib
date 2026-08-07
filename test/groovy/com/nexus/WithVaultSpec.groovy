#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class WithVaultSpec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Registering a mock for the withVault step to simulate Vault integration in tests
        helper.registerAllowedMethod('withVault', [Map.class, Closure.class], { Map m, Closure body ->
            body.delegate = delegate
            return body.call()
        })
    }

    def "test withVault executes successfully and runs closure"() {
        setup:
        def withVaultStep = loadScript('vars/withVault.groovy')
        def closureExecuted = false

        when:
        withVaultStep([
            secrets: [
                [path: 'secret/data/ci/db', engineVersion: 2, secretValues: [[envVar: 'DB_PASSWORD', vaultKey: 'password']]]
            ]
        ]) {
            closureExecuted = true
        }

        then:
        assert closureExecuted == true
        assert helper.callStack.any { it.methodName == 'withVault' }
    }

    def "test withVault fails when secrets parameter is missing"() {
        setup:
        def withVaultStep = loadScript('vars/withVault.groovy')

        when:
        withVaultStep([:]) {
            // No-op
        }

        then:
        thrown(Exception)
    }
}
