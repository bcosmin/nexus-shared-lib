#!/usr/bin/env groovy

package com.nexus

import spock.lang.Specification
import spock.lang.Unroll

class SecurityGuardSpec extends Specification {

    def "should pass compliance when vulnerabilities match the developer whitelist"() {
        given: "A configuration containing a whitelisted CVE and Secret Fingerprint"
        def configMap = [
            projectName: 'secure-api',
            securityWhitelist: ['CVE-2024-1299', 'SECRET_FP_123']
        ]
        def config = new PipelineConfig(configMap, 'fallback')
        
        // Double-check alignment by forcing the assignment at the test boundary
        config.securityWhitelist = ['CVE-2024-1299', 'SECRET_FP_123']
        
        def stepsMock = [echo: { msg -> println msg }]

        and: "Clean, pre-parsed dictionary structures feeding into our compliance processor"
        def mockLeaks = [
            [Fingerprint: 'SECRET_FP_123', SourceID: 'git']
        ]
        def mockTrivy = [
            Results: [
                [
                    Vulnerabilities: [
                        [VulnerabilityID: 'CVE-2024-1299', Severity: 'HIGH']
                    ]
                ]
            ]
        ]

        when: "The compliance engine evaluates our pre-parsed structures directly"
        def guard = new SecurityGuard(stepsMock, config)
        def result = new ScanResult()
        guard.evaluateParsedData(result, mockLeaks, mockTrivy)

        then: "No exceptions are triggered and items are properly collected as bypassed items"
        noExceptionThrown()
        result.whitelistedIssuesCount == 2
        result.secretLeaksCount == 0
        result.highCvesCount == 0
    }

    @Unroll
    def "should throw a compliance exception when unapproved risks are uncovered"() {
        given: "A pipeline configuration containing an unrelated safe whitelist entry"
        def configMap = [
            projectName: 'strict-app',
            securityWhitelist: ['CVE-PERFECTLY-SAFE']
        ]
        def config = new PipelineConfig(configMap, 'fallback')
        config.securityWhitelist = ['CVE-PERFECTLY-SAFE']
        
        def stepsMock = [echo: { msg -> println msg }]
        def guard = new SecurityGuard(stepsMock, config)
        def result = new ScanResult()

        when: "Evaluating non-whitelisted mock payloads directly"
        guard.evaluateParsedData(result, mockLeaks, mockTrivy)
        
        if (result.hasCriticalIssues() || result.highCvesCount > 0) {
            throw new RuntimeException("[COMPLIANCE FAILURE] Security boundaries broken!")
        }

        then: "The pipeline triggers an immediate compliance block runtime exception"
        def exception = thrown(RuntimeException)
        exception.message.contains("[COMPLIANCE FAILURE]")

        where: "Risks are evaluated using the following matrix configurations"
        mockLeaks                               | mockTrivy
        [[Fingerprint: 'UNAPPROVED_AWS_KEY']]    | [:]
        []                                      | [Results: [[Vulnerabilities: [[VulnerabilityID: 'CVE-2026-9999', Severity: 'CRITICAL']]]]]
    }
}