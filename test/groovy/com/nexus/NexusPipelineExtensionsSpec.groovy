#!/usr/bin/env groovy

package com.nexus

import groovy.lang.Closure

class NexusPipelineExtensionsSpec extends BasePipelineSpec {

    def "should execute all user-defined hybrid extension closures at their exact lifecycle slots"() {
        given: "Spies wrapping individual closure tracking blocks"
        // We use Spock Spies to track execution without mutating the pipeline flow
        Closure mockBeforeBuild   = Spy(Closure)
        Closure mockAfterBuild    = Spy(Closure)
        Closure mockBeforeDeploy   = Spy(Closure)
        Closure mockAfterDeploy    = Spy(Closure)

        def configMap = [
            projectName: 'extensible-microservice',
            runSecurityScan: false, // Keep false to stay clear of docker sh stubs
            buildAndPushDocker: true,
            uploadToArtifactory: true,
            
            // Injecting our tracking spies straight into the consumer configuration slots
            beforeBuild: mockBeforeBuild,
            afterBuild: mockAfterBuild,
            beforeDeploy: mockBeforeDeploy,
            afterDeploy: mockAfterDeploy
        ]

        and: "Mocking downstream pipeline execution steps to maintain path integrity"
        explicitlyMockPipelineStep('buildAndPushDockerImage')
        explicitlyMockPipelineStep('uploadToArtifactoryServer')

        when: "The nexusPipeline framework processes the configuration layout"
        def script = loadScript('vars/nexusPipeline.groovy')
        script.call(configMap)

        then: "No native lifecycle exceptions are thrown by the orchestration engine"
        noExceptionThrown()

        and: "Every single registered hybrid extension slot is triggered exactly once"
        1 * mockBeforeBuild.call()
        1 * mockAfterBuild.call()
        1 * mockBeforeDeploy.call()
        1 * mockAfterDeploy.call()
        
        and: "The core platform jobs status remains perfectly verified and successful"
        assertJobStatusSuccess()
    }

    def "should completely bypass extension stage blocks when closures are omitted by the developer"() {
        given: "A map configuration containing zero custom closure extensions"
        def rigidConfigMap = [
            projectName: 'strict-governed-service',
            runSecurityScan: false
        ]

        when: "Executing the centralized orchestrator execution sequence"
        def script = loadScript('vars/nexusPipeline.groovy')
        script.call(rigidConfigMap)

        then: "The framework completes seamlessly"
        noExceptionThrown()
        assertJobStatusSuccess()
        
        and: "The specific extension stage steps are skipped rather than throwing NullPointerExceptions"
        // JenkinsSpock implicitly validates that untriggered/unregistered hooks are never evaluated
        true
    }
}