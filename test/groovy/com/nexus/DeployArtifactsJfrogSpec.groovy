#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class DeployArtifactsJfrogSpec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Register the mock methods for the native rtUpload and rtPublishBuildInfo steps
        helper.registerAllowedMethod('rtUpload', [Map.class], { Map m ->
            return [name: 'build-info-object']
        })
        helper.registerAllowedMethod('rtPublishBuildInfo', [Map.class], null)
    }

    def "test deployArtifactsJfrog executes successfully with required parameters"() {
        setup:
        def deployArtifactsJfrog = loadScript('vars/deployArtifactsJfrog.groovy')

        when:
        def result = deployArtifactsJfrog([
            artifactPath: 'build/libs',
            targetRepo: 'my-custom-repo',
            targetPath: 'com/nexus/app/1.0.0'
        ])

        then:
        assert result.name == 'build-info-object'
        assert helper.callStack.any { it.methodName == 'rtUpload' }
        assert helper.callStack.any { it.methodName == 'rtPublishBuildInfo' }
    }

    def "test deployArtifactsJfrog fails when artifactPath is missing"() {
        setup:
        def deployArtifactsJfrog = loadScript('vars/deployArtifactsJfrog.groovy')

        when:
        deployArtifactsJfrog([
            targetRepo: 'my-custom-repo'
        ])

        then:
        thrown(Exception)
    }
}
