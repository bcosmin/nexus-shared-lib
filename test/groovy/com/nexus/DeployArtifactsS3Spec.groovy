#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class DeployArtifactsS3Spec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Register the mock method for the native s3Upload step
        helper.registerAllowedMethod('s3Upload', [Map.class], null)
    }

    def "test deployArtifactsS3 executes successfully with required parameters"() {
        setup:
        def deployArtifactsS3 = loadScript('vars/deployArtifactsS3.groovy')

        when:
        deployArtifactsS3([
            bucket: 'my-company-artifacts',
            sourcePath: 'build/distributions/app.zip',
            targetPath: 'releases/v1.0.0/'
        ])

        then:
        assert helper.callStack.any { it.methodName == 's3Upload' }
    }

    def "test deployArtifactsS3 fails when bucket is missing"() {
        setup:
        def deployArtifactsS3 = loadScript('vars/deployArtifactsS3.groovy')

        when:
        deployArtifactsS3([
            sourcePath: 'build/distributions/app.zip'
        ])

        then:
        thrown(Exception)
    }

    def "test deployArtifactsS3 fails when sourcePath is missing"() {
        setup:
        def deployArtifactsS3 = loadScript('vars/deployArtifactsS3.groovy')

        when:
        deployArtifactsS3([
            bucket: 'my-company-artifacts'
        ])

        then:
        thrown(Exception)
    }
}
