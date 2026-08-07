#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class SecurityGuardSpec extends BasePipelineTest {

    def setup() {
        super.setUp()
        helper.registerAllowedMethod('echo', [String.class], null)
        helper.registerAllowedMethod('sh', [Map.class], null)
        helper.registerAllowedMethod('readFile', [Map.class], "")
    }

    def "test ScanResult evaluates critical issues correctly"() {
        when:
        def cleanResult = new ScanResult()
        def badResult = new ScanResult(secretLeaksCount: 1)

        then:
        cleanResult.hasCriticalIssues() == false
        badResult.hasCriticalIssues() == true
    }
}
