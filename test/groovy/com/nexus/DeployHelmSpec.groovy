#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class DeployHelmSpec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Înregistrăm metoda mock pentru comanda shell în care se execută helm
        helper.registerAllowedMethod('sh', [String.class], null)
    }

    def "test deployHelm executes successfully with required parameters"() {
        setup:
        def deployHelm = loadScript('vars/deployHelm.groovy')

        when:
        deployHelm([
            releaseName: 'nexus-app',
            chartPath: './charts/nexus-service',
            namespace: 'production',
            setValues: ['image.tag': '1.0.0', 'replicaCount': 2]
        ])

        then:
        assert helper.callStack.any { call ->
            call.methodName == 'sh' &&
            call.args[0].contains('helm upgrade --install nexus-app ./charts/nexus-service') &&
            call.args[0].contains('--namespace production') &&
            call.args[0].contains('--set image.tag=1.0.0') &&
            call.args[0].contains('--atomic')
        }
    }

    def "test deployHelm fails when releaseName is missing"() {
        setup:
        def deployHelm = loadScript('vars/deployHelm.groovy')

        when:
        deployHelm([
            chartPath: './charts/nexus-service'
        ])

        then:
        thrown(Exception)
    }

    def "test deployHelm fails when chartPath is missing"() {
        setup:
        def deployHelm = loadScript('vars/deployHelm.groovy')

        when:
        deployHelm([
            releaseName: 'nexus-app'
        ])

        then:
        thrown(Exception)
    }
}
