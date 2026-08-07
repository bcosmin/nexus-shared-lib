#!/usr/bin/env groovy

package com.nexus

import java.io.Serializable

/**
 * Data transfer object (DTO) holding metadata and execution status
 * resulting from a Docker build and push lifecycle operation.
 */
class DockerResult implements Serializable {

    // Final full image path/coordinates in the registry (e.g., registry.io/nexus/app:v1.0.0)
    String finalImageCoordinates = ''

    // The specific release tag applied to the built image
    String imageTag = ''

    // Raw string representation of the calculated container image size
    String imageSizeRaw = '0MB'

    // Flag indicating whether the container build and push operation completed successfully
    Boolean executionSuccess = false
}
