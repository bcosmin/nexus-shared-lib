#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {
    Logger log = new Logger(this)

    def serverId       = params.serverId ?: 'artifactory-server'
    def targetRepo     = params.targetRepo ?: 'libs-release-local'
    def artifactPath   = params.artifactPath
    def targetPath     = params.targetPath ?: ''
    def includePatterns = params.includePatterns ?: '*'

    if (!artifactPath) {
        log.error('Parameter "artifactPath" is required for Artifactory deployment.')
        error 'JFrog deployment failed: Missing artifactPath.'
    }

    log.info("Preparing to deploy artifacts from [${artifactPath}] to JFrog Artifactory repository [${targetRepo}]...")

    try {
        // Construct the upload configuration for Artifactory
        def rtUpload = Artifactory.newUpload()
        def uploadConfig = [
            serverId: serverId,
            spec: '''{
                "files": [
                    {
                        "pattern": "''' + artifactPath + '/' + includePatterns + '''",
                        "target": "''' + targetRepo + '/' + (targetPath ? targetPath + '/' : '') + '''"
                    }
                ]
            }''',
            collectBuildInfo: true
        ]

        // Execute the upload to Artifactory
        log.info("Uploading artifacts to JFrog Artifactory...")
        def buildInfo = rtUpload uploadConfig

        log.info("Artifacts successfully uploaded to JFrog Artifactory!")

        // Optionally publish build info to Artifactory if specified in params (default is true)
        if (params.publishBuildInfo != null ? params.publishBuildInfo : true) {
            log.info("Publishing build info to Artifactory...")
            rtPublishBuildInfo serverId: serverId, buildInfo: buildInfo
        }

        return buildInfo

    } catch (Exception e) {
        log.error("Failed to deploy artifacts to JFrog Artifactory: ${e.message}")
        error "JFrog deployment failed: ${e.message}"
    }
}
