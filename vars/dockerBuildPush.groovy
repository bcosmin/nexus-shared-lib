#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {
    Logger log = new Logger(this)

    def registry      = params.registry ?: ''
    def imageName     = params.imageName
    def dockerfilePath = params.dockerfilePath ?: 'Dockerfile'
    def buildContext  = params.buildContext ?: '.'
    def credentialsId = params.credentialsId ?: ''
    def tags          = params.tags ?: []

    if (!imageName) {
        log.error('Parameter "imageName" is required for Docker build.')
        error 'Docker build/push failed: Missing imageName.'
    }

    // Generate default tags if none are provided, using the short commit hash and build number
    if (tags.isEmpty()) {
        def shortCommit = env.GIT_COMMIT ? env.GIT_COMMIT.take(7) : 'latest'
        def buildNum    = env.BUILD_NUMBER ?: '1'
        tags = [shortCommit, "build-${buildNum}"]
    }

    // Construct the full image name with registry if provided
    def fullImageName = registry ? "${registry}/${imageName}" : imageName
    def tagArguments = tags.map { "${fullImageName}:${it}" }.collect { "-t ${it}" }.join(' ')

    log.info("Building Docker image [${imageName}] with tags: ${tags.join(', ')}")

    try {
        // Authenticate with Docker registry if credentials are provided
        if (credentialsId && registry) {
            log.info("Authenticating with Docker registry: ${registry}")
            withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASS')]) {
                sh "echo \$REGISTRY_PASS | docker login ${registry} -u \$REGISTRY_USER --password-stdin"
            }
        }

        // Build the Docker image using the specified Dockerfile and context
        def buildCmd = "docker build -f ${dockerfilePath} ${tagArguments} ${buildContext}"
        log.info("Executing: ${buildCmd}")
        sh buildCmd

        // Push each tag to the Docker registry
        log.info("Pushing Docker image tags to registry...")
        tags.each { tag ->
            def targetTag = "${fullImageName}:${tag}"
            sh "docker push ${targetTag}"
            log.info("Successfully pushed: ${targetTag}")
        }

    } catch (Exception e) {
        log.error("Docker build/push failed: ${e.message}")
        error "Docker build/push step failed: ${e.message}"
    }
}
