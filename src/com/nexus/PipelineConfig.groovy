#!/usr/bin/env groovy

package com.nexus

import java.io.Serializable

class PipelineConfig implements Serializable {
    // Project Metadata
    String projectName
    String environment = 'development'
    
    // Feature Toggles
    Boolean runSecurityScan = true
    Boolean optimizeCosts = false
    Boolean uploadArtifactsToS3 = true
    Boolean sendEmailNotifications = true

    // AWS S3 Configuration
    String s3Bucket = 'my-default-bucket'
    String awsRegion = 'us-east-1'
    String awsCredentialsId = 'awsToken' // Jenkins Credential ID for AWS access

    // JFrog Artifactory Configuration
    Boolean uploadToArtifactory = false
    String artifactoryServerId = 'jfrog-enterprise-server'
    String artifactoryTargetRepo = 'generic-local'
    String artifactoryCredentialsId = 'artifactoryToken' // Jenkins Credential ID for Artifactory access
    String artifactoryPattern = '**/*' // Default to uploading all workspace files

    // Container lifecycle configuration
    Boolean buildAndPushDocker = false
    String dockerRegistry = 'index.docker.io/v1/'
    String dockerCredentialsId = 'dockerToken' // Jenkins Credential ID for Docker registry access
    String dockerRepoName = 'my-app'

    // Security Whitelist Configuration (Array of strings like CVE IDs or Secret hashes)
    List<String> securityWhitelist = []

    // Predefined developer execution hooks
    Closure beforeBuild = null
    Closure afterBuild = null
    Closure beforeDeploy = null
    Closure afterDeploy = null
    
    // Constructor accepts the raw map AND a fallback name
    PipelineConfig(Map rawConfig, String fallbackName) {
        
        // Use provided projectName, otherwise fall back to the dynamic default
        this.projectName = rawConfig.projectName ?: fallbackName
    
        if (rawConfig.environment) this.environment = rawConfig.environment

        // AWS S3 settings
        if (rawConfig.s3Bucket) this.s3Bucket = rawConfig.s3Bucket
        if (rawConfig.awsRegion) this.awsRegion = rawConfig.awsRegion
        if (rawConfig.awsCredentialsId) this.awsCredentialsId = rawConfig.awsCredentialsId

        // Artifactory settings
        if (rawConfig.artifactoryServerId) this.artifactoryServerId = rawConfig.artifactoryServerId
        if (rawConfig.artifactoryTargetRepo) this.artifactoryTargetRepo = rawConfig.artifactoryTargetRepo
        if (rawConfig.artifactoryPattern) this.artifactoryPattern = rawConfig.artifactoryPattern
        if (rawConfig.artifactoryCredentialsId) this.artifactoryCredentialsId = rawConfig.artifactoryCredentialsId

        // Docker settings
        if (rawConfig.dockerRegistry) this.dockerRegistry = rawConfig.dockerRegistry
        if (rawConfig.dockerCredentialsId) this.dockerCredentialsId = rawConfig.dockerCredentialsId
        if (rawConfig.dockerRepoName) this.dockerRepoName = rawConfig.dockerRepoName
        

        // Safely map incoming closures if provided by the developer
        if (rawConfig.beforeBuild instanceof Closure) this.beforeBuild = rawConfig.beforeBuild
        if (rawConfig.afterBuild instanceof Closure) this.afterBuild = rawConfig.afterBuild
        if (rawConfig.beforeDeploy instanceof Closure) this.beforeDeploy = rawConfig.beforeDeploy
        if (rawConfig.afterDeploy instanceof Closure) this.afterDeploy = rawConfig.afterDeploy
        
        // Explicit null-checks to preserve boolean defaults
        if (rawConfig.containsKey('runSecurityScan')) {
            this.runSecurityScan = rawConfig.runSecurityScan as Boolean
        }
        if (rawConfig.containsKey('optimizeCosts')) {
            this.optimizeCosts = rawConfig.optimizeCosts as Boolean
        }

        if (rawConfig.containsKey('buildAndPushDocker')) {
            this.buildAndPushDocker = rawConfig.buildAndPushDocker as Boolean
        }
        if (rawConfig.containsKey('uploadArtifactsToS3')) {
            this.uploadArtifactsToS3 = rawConfig.uploadArtifactsToS3 as Boolean
        }

        if (rawConfig.containsKey('uploadToArtifactory')) {
            this.uploadToArtifactory = rawConfig.uploadToArtifactory as Boolean
        }

        if (rawConfig.containsKey('sendEmailNotifications')) {
            this.sendEmailNotifications = rawConfig.sendEmailNotifications as Boolean
        }
    }
}