#!/usr/bin/env groovy

package com.nexus

class PipelineConfig implements Serializable {

    // General configuration
    String projectName
    String environment
    String branch
    String gitCredentialsId

    // Security & Code Quality
    boolean runSecurityScan
    boolean runAdvancedSecurityGuard
    String buildTool
    String sonarAdditionalArgs

    // Build & Containerization
    boolean buildAndPushDocker
    String dockerRegistry
    String dockerImageName
    String dockerfilePath
    String dockerCredentialsId

    // S3 Artifact Distribution
    boolean uploadArtifactsToS3
    String s3Bucket
    String s3SourcePath
    String awsRegion
    String awsCredentialsId

    // JFrog Artifactory Distribution
    boolean uploadToArtifactory
    String artifactoryServerId
    String artifactoryTargetRepo
    String artifactoryArtifactPath

    // Kubernetes & Helm Deployment
    boolean deployToK8s
    String helmReleaseName
    String helmChartPath
    String helmNamespace
    Map helmSetValues

    // Custom Extension Hooks (Closures)
    Closure beforeBuild
    Closure afterBuild
    Closure beforeDeploy
    Closure afterDeploy

    // Notifications
    boolean sendEmailNotifications
    String notificationEmail
    boolean notifyOnSuccess
    
    boolean sendSlackNotification
    String slackChannel

    boolean sendTeamsNotification
    String teamsWebhookUrl

    /**
     * Constructor that hydrates the configuration map provided in the Jenkinsfile.
     * @param configMap The map passed from the Jenkinsfile pipeline call
     * @param fallbackProjectName Dynamic project name fallback if not specified
     */
    PipelineConfig(Map configMap = [:], String fallbackProjectName = 'nexus-unknown-fallback') {
        this.projectName         = configMap.projectName ?: fallbackProjectName
        this.environment         = configMap.environment ?: 'development'
        this.branch              = configMap.branch ?: 'main'
        this.gitCredentialsId    = configMap.gitCredentialsId ?: ''

        this.runSecurityScan     = configMap.runSecurityScan != null ? configMap.runSecurityScan : false
        this.buildTool           = configMap.buildTool ?: 'gradle'
        this.sonarAdditionalArgs = configMap.sonarAdditionalArgs ?: ''

        this.buildAndPushDocker  = configMap.buildAndPushDocker != null ? configMap.buildAndPushDocker : false
        this.dockerRegistry      = configMap.dockerRegistry ?: ''
        this.dockerImageName     = configMap.dockerImageName ?: this.projectName
        this.dockerfilePath      = configMap.dockerfilePath ?: 'Dockerfile'
        this.dockerCredentialsId = configMap.dockerCredentialsId ?: ''

        this.uploadArtifactsToS3 = configMap.uploadArtifactsToS3 != null ? configMap.uploadArtifactsToS3 : false
        this.s3Bucket            = configMap.s3Bucket ?: ''
        this.s3SourcePath        = configMap.s3SourcePath ?: 'build/libs'
        this.awsRegion           = configMap.awsRegion ?: 'eu-central-1'
        this.awsCredentialsId    = configMap.awsCredentialsId ?: 'aws-s3-credentials'

        this.uploadToArtifactory = configMap.uploadToArtifactory != null ? configMap.uploadToArtifactory : false
        this.artifactoryServerId = configMap.artifactoryServerId ?: 'artifactory-server'
        this.artifactoryTargetRepo = configMap.artifactoryTargetRepo ?: 'libs-release-local'
        this.artifactoryArtifactPath = configMap.artifactoryArtifactPath ?: 'build/libs'

        this.deployToK8s         = configMap.deployToK8s != null ? configMap.deployToK8s : false
        this.helmReleaseName     = configMap.helmReleaseName ?: this.projectName
        this.helmChartPath       = configMap.helmChartPath ?: './charts'
        this.helmNamespace       = configMap.helmNamespace ?: 'default'
        this.helmSetValues       = configMap.helmSetValues ?: [:]

        this.beforeBuild         = configMap.beforeBuild
        this.afterBuild          = configMap.afterBuild
        this.beforeDeploy        = configMap.beforeDeploy
        this.afterDeploy         = configMap.afterDeploy

        this.sendEmailNotifications = configMap.sendEmailNotifications != null ? configMap.sendEmailNotifications : false
        this.notificationEmail   = configMap.notificationEmail ?: ''
        this.notifyOnSuccess     = configMap.notifyOnSuccess != null ? configMap.notifyOnSuccess : false
        
        this.sendSlackNotification = configMap.sendSlackNotification != null ? configMap.sendSlackNotification : false
        this.slackChannel        = configMap.slackChannel ?: ''

        this.sendTeamsNotification = configMap.sendTeamsNotification != null ? configMap.sendTeamsNotification : false
        this.teamsWebhookUrl     = configMap.teamsWebhookUrl ?: ''
    }
}
