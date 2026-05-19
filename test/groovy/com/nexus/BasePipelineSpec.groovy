#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import spock.lang.Specification

/**
 * Common architectural unit test framework layer that provides automated
 * Jenkins DSL step mocking contexts to our individual pipeline tests.
 */
class BasePipelineSpec extends DeclarativePipelineTest {

    // Runs before every single individual feature test block execution loop
    def setup() {
        // Initialize the LesFurets internal pipeline registry state maps
        super.setUp()

        // Configure the search boundaries to find your global orchestrators
        this.scriptRoots = ['vars']

        // Mock essential runtime environment states natively injected by Jenkins
        def envMock = [
            JOB_BASE_NAME: 'nexus-billing-service',
            BUILD_NUMBER: '42',
            BRANCH_NAME: 'main'
        ]
        binding.setVariable('env', envMock)

        // Mock out our custom logging mechanisms globally to prevent console cluttering
        def loggerMock = Mock(Logger)
        binding.setVariable('Logger', loggerMock)

        // Register standard third-party pipeline steps to prevent execution exceptions
        registerAllowedMethod('withAWS', [Map, Closure], null)
        registerAllowedMethod('s3Upload', [Map], null)
        registerAllowedMethod('timestamps', [Closure], { Closure c -> c() })
        registerAllowedMethod('timeout', [Map, Closure], { Map m, Closure c -> c() })
        registerAllowedMethod('buildDiscarder', [Object], null)
        registerAllowedMethod('logRotator', [Map], null)
        registerAllowedMethod('sendEmail', [Map], null)
    }
}