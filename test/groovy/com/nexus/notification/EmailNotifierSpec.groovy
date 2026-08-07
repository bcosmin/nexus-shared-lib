#!/usr/bin/env groovy

package com.nexus.notification

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class EmailNotifierSpec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Register a mock for the native emailext step used in EmailNotifier
        helper.registerAllowedMethod('emailext', [Map.class], null)
    }

    def "test sendNotification executes emailext when emailRecipients are provided"() {
        setup:
        def emailNotifier = new EmailNotifier(this)

        when:
        emailNotifier.sendNotification([
            emailRecipients: 'team@company.com',
            jobName: 'nexus-service',
            buildNumber: '42',
            status: 'SUCCESS'
        ], 'Build completed without errors.')

        then:
        // Verify that the emailext step was called with the right recipients and subject
        assert helper.callStack.any { call ->
            call.methodName == 'emailext' &&
            call.args[0].to == 'team@company.com' &&
            call.args[0].subject.contains('nexus-service')
        }
    }

    def "test sendNotification skips execution when recipients are missing"() {
        setup:
        def emailNotifier = new EmailNotifier(this)

        when:
        emailNotifier.sendNotification([
            jobName: 'nexus-service',
            status: 'FAILURE'
        ], 'Build failed.')

        then:
        // Verify that emailext was never called since no recipient was supplied
        assert !helper.callStack.any { it.methodName == 'emailext' }
    }
}
