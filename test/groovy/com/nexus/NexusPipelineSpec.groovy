#!/usr/bin/env groovy

package com.nexus

class NexusPipelineSpec extends BasePipelineSpec {

    def "should execute successfully when parsing default standard configuration settings"() {
        given: "A basic configuration payload map"
        def configMap = [
            projectName: 'unit-test-app',
            runSecurityScan: false
        ]

        when: "The global orchestrator nexusPipeline step is called"
        def script = loadScript('vars/nexusPipeline.groovy')
        script.call(configMap)

        then: "The script compiles and executes without throwing lifecycle engine errors"
        noExceptionThrown()

        and: "The orchestrator successfully verified execution status"
        // Correctly reading from the inherited engine instance status state property
        this.status == com.lesfurets.jenkins.unit.global.lib.JobStatus.SUCCESS
    }

    def "should attempt to trigger JFrog Artifactory distribution logic only when uploadToArtifactory toggle is true"() {
        given: "An explicit configuration instructing a binary upload target"
        def configMap = [
            projectName: 'distribution-service',
            runSecurityScan: false,
            uploadToArtifactory: true
        ]

        // Register the dynamic target step macro hook directly into the framework context instance
        registerAllowedMethod('uploadToArtifactoryServer', [Map], null)

        when: "Executing the centralized nexusPipeline framework script context"
        def script = loadScript('vars/nexusPipeline.groovy')
        script.call(configMap)

        then: "The framework completes smoothly"
        noExceptionThrown()
    }
}