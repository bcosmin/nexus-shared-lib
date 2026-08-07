#!/usr/bin/env groovy

package com.nexus

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class DockerBuildPushSpec extends BasePipelineTest {

    def setup() {
        super.setUp()
        binding.setVariable('env', [
            'GIT_COMMIT': 'abcdef1234567890',
            'BUILD_NUMBER': '42'
        ])

        // Înregistrăm metodele mock pentru shell și blocul de credențiale
        helper.registerAllowedMethod('sh', [String.class], null)
        helper.registerAllowedMethod('withCredentials', [List.class, Closure.class], { List creds, Closure body ->
            body.delegate = delegate
            return body.call()
        })
    }

    def "test dockerBuildPush executes successfully with custom tags and registry"() {
        setup:
        def dockerBuildPush = loadScript('vars/dockerBuildPush.groovy')

        when:
        dockerBuildPush([
            registry: 'my-registry.io',
            imageName: 'nexus/my-app',
            tags: ['1.0.0', 'latest']
        ])

        then:
        assert helper.callStack.any { it.methodName == 'sh' && it.args[0].contains('docker build') }
        assert helper.callStack.any { it.methodName == 'sh' && it.args[0].contains('docker push my-registry.io/nexus/my-app:latest') }
    }

    def "test dockerBuildPush uses default dynamic tags when not provided"() {
        setup:
        def dockerBuildPush = loadScript('vars/dockerBuildPush.groovy')

        when:
        dockerBuildPush([
            imageName: 'nexus/my-app'
        ])

        then:
        assert helper.callStack.any { it.methodName == 'sh' && it.args[0].contains('docker build') }
        assert helper.callStack.any { it.methodName == 'sh' && it.args[0].contains('docker push nexus/my-app:abcdef1') }
    }

    def "test dockerBuildPush fails when imageName is missing"() {
        setup:
        def dockerBuildPush = loadScript('vars/dockerBuildPush.groovy')

        when:
        dockerBuildPush([
            registry: 'my-registry.io'
        ])

        then:
        thrown(Exception)
    }
}
