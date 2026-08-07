#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class NexusPipelineSpec extends BasePipelineTest {

    void setup() {
        super.setUp()
        binding.setVariable('env', [
            JOB_NAME: 'test-pipeline',
            BUILD_NUMBER: '1',
            BUILD_URL: 'http://localhost/job/test-pipeline/1/'
        ])

        // Înregistrăm metodele globale/shared library mock-uite pentru a nu da erori la apel
        helper.registerAllowedMethod('scmCheckout', [Map.class], { return [GIT_COMMIT: 'abcdef123456'] })
        helper.registerAllowedMethod('sonarqubeScan', [Map.class], { return null })
        helper.registerAllowedMethod('notifyBuildStatus', [Map.class], { return null })
    }

    def "should execute nexusPipeline and invoke security and checkout steps when enabled"() {
        when:
        loadScript('vars/nexusPipeline.groovy').call([
            projectName: 'demo-app',
            environment: 'dev',
            buildTool: 'gradle',
            runSecurityScan: true,
            buildAndPushDocker: false,
            uploadToArtifactory: false,
            deployToK8s: false
        ])

        then:
        // Verificăm că pașii au fost efectivamente apelați în timpul execuției pipeline-ului
        assert helper.callStack.findAll { it.methodName == 'scmCheckout' }.size() > 0
        assert helper.callStack.findAll { it.methodName == 'sonarqubeScan' }.size() > 0
        jobStatus == 'SUCCESS'
    }
}
