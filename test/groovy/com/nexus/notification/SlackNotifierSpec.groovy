#!/usr/bin/env groovy

package com.nexus.notification

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class SlackNotifierSpec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Register a mock for the native slackSend step used in SlackNotifier
        helper.registerAllowedMethod('slackSend', [Map.class], null)
    }

    def "test sendNotification executes slackSend when slackChannel is provided"() {
        setup:
        def slackNotifier = new SlackNotifier(this)

        when:
        slackNotifier.sendNotification([
            slackChannel: '#devops-alerts',
            color: 'good'
        ], 'Pipeline build succeeded.')

        then:
        // Verify that the slackSend step was invoked with the correct parameters
        assert helper.callStack.any { call ->
            call.methodName == 'slackSend' &&
            call.args[0].channel == '#devops-alerts' &&
            call.args[0].color == 'good'
        }
    }

    def "test sendNotification skips execution when slackChannel is missing"() {
        setup:
        def slackNotifier = new SlackNotifier(this)

        when:
        slackNotifier.sendNotification([
            color: 'danger'
        ], 'Pipeline build failed.')

        then:
        // Verify that slackSend was never called since channel is missing
        assert !helper.callStack.any { it.methodName == 'slackSend' }
    }
}
