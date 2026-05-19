#!/usr/bin/env groovy

package com.nexus

import spock.lang.Specification

class PipelineConfigSpec extends Specification {

    def "should correctly populate object parameters using a provided valid map configuration"() {
        given: "A custom user configuration map"
        def rawMap = [
            projectName: 'core-payments-api',
            environment: 'production',
            runSecurityScan: false,
            optimizeCosts: true,
            uploadToArtifactory: true
        ]

        when: "The map is passed to the PipelineConfig constructor object"
        def config = new PipelineConfig(rawMap, 'fallback-name')

        then: "The attributes inside the object match our expectations"
        config.projectName == 'core-payments-api'
        config.environment == 'production'
        config.runSecurityScan == false
        config.optimizeCosts == true
        config.uploadToArtifactory == true

        and: "Unspecified platform attributes match their actual default values"
        // FIXED: Swapped to true to align directly with your class field configuration default value
        config.uploadArtifactsToS3 == true
        config.awsRegion == 'us-east-1'
    }

    def "should gracefully trigger the fallback project string name when maps omit it"() {
        given: "A configuration missing the project identifier"
        def emptyMap = [:]

        when: "Constructing the configurations object with an explicit fallback string context"
        def config = new PipelineConfig(emptyMap, 'dynamic-jenkins-fallback-id')

        then: "The framework correctly adopts the fallback identifier mapping"
        config.projectName == 'dynamic-jenkins-fallback-id'
    }
}