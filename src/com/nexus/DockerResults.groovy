#!/usr/bin/env groovy

package com.nexus

import java.io.Serializable

class DockerResult implements Serializable {
    String finalImageCoordinates = ''
    String imageTag = ''
    String imageSizeRaw = '0MB'
    Boolean executionSuccess = false
}