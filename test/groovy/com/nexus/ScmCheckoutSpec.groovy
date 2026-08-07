#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class ScmCheckoutSpec extends BasePipelineTest {

    def setup() {
        super.setUp()
        binding.setVariable('env', [
            'BRANCH_NAME': 'main',
            'GIT_URL': 'https://github.com/nexus/example-repo.git'
        ])

        // Mock the checkout method to simulate a successful checkout
        helper.registerAllowedMethod('checkout', [Map.class], { Map m ->
            return [GIT_COMMIT: 'abcdef1234567890']
        })
    }

    def "test scmCheckout executes successfully with default parameters"() {
        setup:
        def scmCheckout = loadScript('vars/scmCheckout.groovy')

        when:
        def result = scmCheckout()

        then:
        assert result.GIT_COMMIT == 'abcdef1234567890'
        assert helper.callStack.any { it.methodName == 'checkout' }
    }
}
