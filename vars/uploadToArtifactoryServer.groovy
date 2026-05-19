#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {
    def log = new Logger(this)
    
    def serverId     = params.serverId
    def targetRepo   = params.targetRepo
    def pattern      = params.pattern
    def credentialsId = params.credentialsId ?: 'jfrog-platform-token' // Dynamic fallback
    def targetPath   = params.targetPath ?: "${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}/"

    if (!serverId || !targetRepo || !pattern) {
        log.error('JFrog Artifactory upload failed: Missing mandatory structural parameters.')
        error 'Artifactory Step Misconfigured'
    }

    log.info("Connecting to JFrog Artifactory instance [${serverId}] using dynamic Credential ID [${credentialsId}]")

    // Retrieve server definition and safely inject credentials context dynamically
    def server = Artifactory.server(serverId)
    server.credentialsId = credentialsId
    
    def uploadSpec = """{
        "files": [
            {
                "pattern": "${pattern}",
                "target": "${targetRepo}/${targetPath}"
            }
        ]
    }"""

    try {
        log.info("Uploading workspace artifacts matching [${pattern}] directly to repo [${targetRepo}]...")
        
        // Execute the native authenticated upload
        def buildInfo = server.upload(uploadSpec)
        
        buildInfo.env.capture = true
        server.publishBuildInfo(buildInfo)
        
        log.info('JFrog Artifactory distribution completed successfully.')
    } catch (Exception e) {
        log.error("Artifactory Plugin execution failure: ${e.message}")
        error "Pipeline halted: Unable to distribute binaries to Artifactory. Reason: ${e.message}"
    }
}