#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class SonarQubeScanSpec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Înregistrăm metodele blocurilor specifice SonarQube și Jenkins Pipeline
        helper.registerAllowedMethod('withSonarQubeEnv', [String.class, Closure.class], { String name, Closure body ->
            body.delegate = delegate
            return body.call()
        })
        helper.registerAllowedMethod('sh', [String.class], null)
        helper.registerAllowedMethod('timeout', [Map.class, Closure.class], { Map m, Closure body ->
            return body.call()
        })
        helper.registerAllowedMethod('waitForQualityGate', [], {
            return [status: 'OK']
        })
    }

    def "test sonarqubeScan executes successfully with default parameters"() {
        setup:
        def sonarqubeScan = loadScript('vars/sonarqubeScan.groovy')

        when:
        sonarqubeScan([
            buildTool: 'maven'
        ])

        then:
        assert helper.callStack.any { it.methodName == 'withSonarQubeEnv' }
        assert helper.callStack.any { it.methodName == 'sh' && it.args[0] == 'mvn sonar:sonar' }
        assert helper.callStack.any { it.methodName == 'waitForQualityGate' }
    }
}
