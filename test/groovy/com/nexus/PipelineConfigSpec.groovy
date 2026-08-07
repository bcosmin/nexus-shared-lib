#!/usr/bin/env groovy

package com.nexus

import spock.lang.Specification

class PipelineConfigSpec extends Specification {

    def "test PipelineConfig loads custom values and defaults correctly"() {
        when:
        def config = new PipelineConfig([
            projectName: 'my-test-app',
            environment: 'production',
            runSecurityScan: true,
            buildTool: 'maven'
        ], 'fallback-name')

        then:
        config.projectName == 'my-test-app'
        config.environment == 'production'
        config.runSecurityScan == true
        config.buildTool == 'maven'
        // Verificăm un default
        config.awsRegion == 'eu-central-1'
        config.buildAndPushDocker == false
    }

    def "test PipelineConfig uses fallback when parameters are missing"() {
        when:
        def config = new PipelineConfig([:], 'fallback-name')

        then:
        config.projectName == 'fallback-name'
        config.environment == 'development'
        config.buildTool == 'gradle'
    }
}
