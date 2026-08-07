#!/usr/bin/env groovy

package com.nexus

import spock.lang.Specification

class DockerResultSpec extends Specification {

    def "test DockerResult initializes with correct default values"() {
        when:
        def result = new DockerResult()

        then:
        result.finalImageCoordinates == ''
        result.imageTag == ''
        result.imageSizeRaw == '0MB'
        result.executionSuccess == false
    }

    def "test DockerResult holds assigned values correctly"() {
        when:
        def result = new DockerResult(
            finalImageCoordinates: 'my-registry.io/nexus/app:42',
            imageTag: '42',
            imageSizeRaw: '75MB',
            executionSuccess: true
        )

        then:
        result.finalImageCoordinates == 'my-registry.io/nexus/app:42'
        result.imageTag == '42'
        result.imageSizeRaw == '75MB'
        result.executionSuccess == true
    }
}
