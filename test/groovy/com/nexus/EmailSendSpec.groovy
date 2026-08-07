#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class EmailSendSpec extends BasePipelineTest {

    def setup() {
        super.setUp()
        binding.setVariable('env', [
            'JOB_NAME': 'nexus-service/main',
            'BUILD_NUMBER': '10',
            'BUILD_URL': 'http://jenkins.local/job/nexus-service/10/'
        ])
        binding.setVariable('currentBuild', [
            'currentResult': 'SUCCESS'
        ])

        helper.registerAllowedMethod('emailext', [Map.class], null)
    }

    def "test emailSend triggers emailext with correct defaults and template"() {
        setup:
        def emailSend = loadScript('vars/sendEmail.groovy')

        when:
        emailSend([
            recipients: ['admin@example.com']
        ])

        then:
        assert helper.callStack.any { call ->
            call.methodName == 'emailext' &&
            call.args[0].to == 'admin@example.com' &&
            call.args[0].subject.contains('nexus-service/main #10') &&
            call.args[0].mimeType == 'text/html' &&
            call.args[0].body.contains('<!DOCTYPE html>')
        }
    }
}
