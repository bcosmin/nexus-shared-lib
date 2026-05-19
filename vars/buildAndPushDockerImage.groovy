#!/usr/bin/env groovy

import com.nexus.Logger
import com.nexus.DockerResult

def call(Map params = [:]) {
    def log = new Logger(this)
    def result = new DockerResult()
    
    def registry      = params.registry ?: 'index.docker.io/v1/'
    def repoName      = params.repoName
    def credentialsId = params.credentialsId ?: 'docker-registry-token'
    def imageTag      = params.tag ?: "${env.BUILD_NUMBER}"

    if (!repoName) {
        log.error('Docker Build engine failure: Target repository name parameter is completely missing.')
        error 'Docker Architectural Misconfiguration'
    }

    String fullCoordinates = "${repoName}:${imageTag}"
    log.info("Opening authenticated Docker daemon stream for registry target: ${registry}")

    // Execute the authenticated docker workspace context block via native plugin structures
    docker.withRegistry(registry, credentialsId) {
        try {
            log.info("Compiling container architecture matching coordinate tags: [${fullCoordinates}]")
            
            // Build image using the root local directory Dockerfile
            def customImage = docker.build(fullCoordinates, ".")
            
            log.info('Publishing image registry layer upstream...')
            customImage.push()
            
            if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') {
                log.info("Production trunk branch identified. Publishing supplemental 'latest' tracker tag...")
                customImage.push('latest')
            }

            // EXTRACT METADATA: Safely pull image size tracking data from local docker engine storage
            try {
                String sizeOutput = sh(
                    script: "docker images --format '{{.Size}}' ${repoName}:${imageTag}", 
                    returnStdout: true
                ).trim()
                result.imageSizeRaw = sizeOutput ?: 'Unknown'
            } catch (Exception sizeEx) {
                log.warn("Unable to map container image size data metrics: ${sizeEx.message}")
            }

            // Hydrate the return payload metrics
            result.finalImageCoordinates = fullCoordinates
            result.imageTag = imageTag
            result.executionSuccess = true
            
            log.info("Container deployment complete. Recorded Compiled Size: ${result.imageSizeRaw}")
            
        } catch (Exception e) {
            log.error("Docker runtime compilation failure: ${e.message}")
            error "Pipeline halted: Container compilation or distribution failed. Reason: ${e.message}"
        }
    }
    
    return result
}