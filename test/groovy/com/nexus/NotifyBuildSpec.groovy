#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class NotifyBuildSpec extends BasePipelineTest {

    def setup() {
        super.setUp()
        binding.setVariable('env', [
            'JOB_NAME': 'nexus-service/main',
            'BUILD_NUMBER': '10',
            'BUILD_URL': 'http://jenkins.local/job/nexus-service/10/'
        ])

        helper.registerAllowedMethod('slackSend', [Map.class], null)
        helper.registerAllowedMethod('httpRequest', [Map.class], null)
        helper.registerAllowedMethod('emailext', [Map.class], null)
    }

    def "test notification triggers only requested channels"() {
        setup:
        def notifyBuild = loadScript('vars/notifyBuild.groovy')

        when:
        notifyBuild([
            status: 'SUCCESS',
            channels: ['teams', 'email'],
            teamsWebhookUrl: 'https://fake-teams-webhook',
            emailTo: 'test@example.com',
            message: 'Pipeline deployment passed!'
        ])

        then:
        assert helper.callStack.any { it.methodName == 'httpRequest' }
        assert helper.callStack.any { it.methodName == 'emailext' }
        !helper.callStack.any { it.methodName == 'slackSend' }
    }
}
