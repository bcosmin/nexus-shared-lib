#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class UploadToS3Spec extends BasePipelineTest {

    def setup() {
        super.setUp()

        // Register the mock method for the native s3Upload step
        helper.registerAllowedMethod('s3Upload', [Map.class], null)
    }

    def "test uploadToS3 executes successfully with required parameters"() {
        setup:
        def uploadToS3 = loadScript('vars/uploadToS3.groovy')

        when:
        uploadToS3([
            bucket: 'my-reports-bucket',
            file: 'build/reports/**/*',
            targetPath: 'junit-reports/v1/'
        ])

        then:
        assert helper.callStack.any { it.methodName == 's3Upload' }
    }

    def "test uploadToS3 fails when bucket is missing"() {
        setup:
        def uploadToS3 = loadScript('vars/uploadToS3.groovy')

        when:
        uploadToS3([
            file: 'build/reports/**/*'
        ])

        then:
        thrown(Exception)
    }

    def "test uploadToS3 fails when file is missing"() {
        setup:
        def uploadToS3 = loadScript('vars/uploadToS3.groovy')

        when:
        uploadToS3([
            bucket: 'my-reports-bucket'
        ])

        then:
        thrown(Exception)
    }
}
