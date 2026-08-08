#!/usr/bin/env groovy

import com.nexus.PipelineConfig
import com.nexus.Logger
import com.nexus.SecurityGuard
import com.nexus.notification.EmailNotifier
import com.nexus.notification.SlackNotifier
import com.nexus.notification.TeamsNotifier

def call(Map configMap = [:]) {
    // Instantiate the centralized logger inside the active pipeline execution context
    def log = new Logger(this)

    // Fallback project name derived from Jenkins job base name if not explicitly provided
    String fallbackProjectName = env.JOB_BASE_NAME ?: 'nexus-unknown-fallback'

    // Hydrate configuration object with user map and fallback defaults
    def config = new PipelineConfig(configMap, fallbackProjectName)

    // Warn gracefully if project name fallback was triggered
    if (config.projectName == 'nexus-unknown-fallback') {
        log.warn(" 'projectName' was not defined in your Jenkinsfile. Using dynamic fallback: ${config.projectName}")
    }

    // Define the core declarative pipeline structure
    pipeline {
        agent any

        // Global execution options and guardrails
        options {
            timestamps()
            timeout(time: 1, unit: 'HOURS')
            buildDiscarder(logRotator(numToKeepStr: '10'))
            wrap([$class: 'AnsiColorBuildWrapper', colorMapName: 'xterm'])
        }

        stages {
            // Stage 1: Initialization and Code Retrieval
            stage('Initialization & Checkout') {
                steps {
                    script {
                        log.info("Initializing pipeline for Project: ${config.projectName}")
                        log.info("Target Deployment Environment: ${config.environment}")

                        // Execute standardized SCM checkout step
                        scmCheckout(
                            branch: config.branch ?: env.BRANCH_NAME ?: 'main',
                            credentialsId: config.gitCredentialsId ?: ''
                        )
                    }
                }
            }

            // Stage 2: Static Code Analysis & Security Compliance (SonarQube)
            stage('Security Compliance Scan') {
                when {
                    // Evaluate flag to determine whether to trigger security scan
                    expression { return config.runSecurityScan }
                }
                steps {
                    script {
                        log.info("Starting SonarQube quality and security analysis...")
                        sonarqubeScan(
                            buildTool: config.buildTool ?: 'gradle',
                            additionalArguments: config.sonarAdditionalArgs ?: ''
                        )
                    }
                }
            }

            // Stage 3: Advanced Security Guard (Trufflehog & Trivy)
            stage('Advanced Security Guard') {
                when {
                    expression { return config.runAdvancedSecurityGuard }
                }
                steps {
                    script {
                        log.info("Executing advanced security guard compliance scan...")
                        def securityGuard = new SecurityGuard(this, config)
                        def scanResult = securityGuard.runComplianceScan()

                        log.info("""
                            ==================================================
                            SECURITY COMPLIANCE AUDIT SCORECARD: ${config.projectName.toUpperCase()}
                            ==================================================
                            - Secret Leaks Found:      ${scanResult.secretLeaksCount}
                            - Critical CVEs Found:     ${scanResult.criticalCvesCount}
                            - High CVEs Found:         ${scanResult.highCvesCount}
                            - Whitelisted Issues:      ${scanResult.whitelistedIssuesCount}
                            ==================================================
                        """.stripIndent())
                    }
                }
            }

            // Stage 4: Custom Pre-Build Extensions Slot
            stage('Pre-Build Extensions') {
                when {
                    // Execute only if a custom closure was injected from the Jenkinsfile
                    expression { return config.beforeBuild != null }
                }
                steps {
                    script {
                        log.info("Executing user-defined [beforeBuild] tasks...")
                        config.beforeBuild.delegate = this
                        config.beforeBuild()
                    }
                }
            }

            // Stage 5: Standardized Application Build
            stage('Build Application') {
                steps {
                    script {
                        log.info("Building application: ${config.projectName}")
                        // Dynamically resolve build command based on the configured tool stack
                        def buildCommand = config.buildTool == 'maven' ? 'mvn clean package' : './gradlew clean build'
                        sh buildCommand
                    }
                }
            }

            // Stage 6: Container Image Lifecycle (Build & Push)
            stage('Docker Build and Push') {
                when {
                    expression { return config.buildAndPushDocker }
                }
                steps {
                    script {
                        log.info("Building and pushing Docker image for ${config.projectName}...")
                        def containerReport = dockerBuildPush(
                            registry: config.dockerRegistry,
                            imageName: config.dockerImageName ?: config.projectName,
                            dockerfilePath: config.dockerfilePath ?: 'Dockerfile',
                            credentialsId: config.dockerCredentialsId
                        )

                        log.info("""
                            ==================================================
                            CONTAINER DISTRIBUTION SCORECARD: ${config.projectName.toUpperCase()}
                            ==================================================
                            - Image Distribution Path: ${containerReport.finalImageCoordinates}
                            - Target Registry Domain:  ${config.dockerRegistry}
                            - Manifest Release Tag:    ${containerReport.imageTag}
                            - Calculated Layer Size:   ${containerReport.imageSizeRaw}
                            ==================================================
                        """.stripIndent())
                    }
                }
            }

            // Stage 7: Binary / Artifact Distribution to AWS S3
            stage('Upload Artifacts to S3') {
                when {
                    expression { return config.uploadArtifactsToS3 }
                }
                steps {
                    script {
                        log.info("Uploading build artifacts to S3...")
                        deployArtifactsS3(
                            bucket: config.s3Bucket,
                            sourcePath: config.s3SourcePath ?: 'build/libs',
                            targetPath: "${config.projectName}/${env.BUILD_NUMBER}/",
                            region: config.awsRegion ?: 'eu-central-1',
                            awsCredential: config.awsCredentialsId ?: 'aws-s3-credentials'
                        )
                    }
                }
            }

            // Stage 8: Binary Distribution to JFrog Artifactory
            stage('Upload Artifacts to Artifactory') {
                when {
                    expression { return config.uploadToArtifactory }
                }
                steps {
                    script {
                        log.info("Uploading artifacts to JFrog Artifactory...")
                        deployArtifactsJfrog(
                            serverId: config.artifactoryServerId ?: 'artifactory-server',
                            targetRepo: config.artifactoryTargetRepo ?: 'libs-release-local',
                            artifactPath: config.artifactoryArtifactPath ?: 'build/libs',
                            targetPath: "${config.projectName}/${env.BUILD_NUMBER}"
                        )
                    }
                }
            }

            // Stage 9: Kubernetes Deployment protected by Safe Rollback mechanism
            stage('Deploy to Kubernetes (Helm)') {
                when {
                    expression { return config.deployToK8s }
                }
                steps {
                    script {
                        log.info("Deploying Helm release [${config.helmReleaseName}]...")

                        // Wrap deployment inside safeRollback to automatically recover on failure
                        safeRollback(releaseName: config.helmReleaseName, namespace: config.helmNamespace ?: 'default') {
                            deployHelm(
                                releaseName: config.helmReleaseName,
                                chartPath: config.helmChartPath,
                                namespace: config.helmNamespace ?: 'default',
                                setValues: config.helmSetValues ?: [:]
                            )
                        }
                    }
                }
            }
        }

        // Post-execution hooks for logging, cleanup, and multi-channel notifications
        post {
            always {
                script {
                    log.info("Pipeline execution finished for Project: ${config.projectName}")
                }
            }
            failure {
                script {
                    log.error("Pipeline failed for Project: ${config.projectName}")
                    String failureMsg = "Pipeline failed in environment: ${config.environment}. Check console logs at ${env.BUILD_URL}"

                    // 1. Email Notification
                    if (config.notificationEmail) {
                        def emailNotifier = new EmailNotifier(this)
                        emailNotifier.sendNotification([
                            emailRecipients: config.notificationEmail,
                            jobName: config.projectName,
                            buildNumber: env.BUILD_NUMBER,
                            status: 'FAILURE'
                        ], failureMsg)
                    }

                    // 2. Slack Notification
                    if (config.slackChannel) {
                        def slackNotifier = new SlackNotifier(this)
                        slackNotifier.sendNotification([
                            slackChannel: config.slackChannel,
                            color: 'danger'
                        ], "CRITICAL FAILURE: Project *${config.projectName}* build #${env.BUILD_NUMBER} failed.")
                    }

                    // 3. Microsoft Teams Notification
                    if (config.teamsWebhookUrl) {
                        def teamsNotifier = new TeamsNotifier(this)
                        teamsNotifier.sendNotification([
                            teamsWebhookUrl: config.teamsWebhookUrl,
                            status: 'FAILURE',
                            colorHex: 'dc3545'
                        ], failureMsg)
                    }
                }
            }
            success {
                script {
                    log.info("Pipeline succeeded for Project: ${config.projectName}")
                    String successMsg = "Pipeline completed successfully for environment: ${config.environment}."

                    if (config.notifyOnSuccess) {
                        // 1. Email Notification
                        if (config.notificationEmail) {
                            def emailNotifier = new EmailNotifier(this)
                            emailNotifier.sendNotification([
                                emailRecipients: config.notificationEmail,
                                jobName: config.projectName,
                                buildNumber: env.BUILD_NUMBER,
                                status: 'SUCCESS'
                            ], successMsg)
                        }

                        // 2. Slack Notification
                        if (config.slackChannel) {
                            def slackNotifier = new SlackNotifier(this)
                            slackNotifier.sendNotification([
                                slackChannel: config.slackChannel,
                                color: 'good'
                            ], "SUCCESS: Project *${config.projectName}* build #${env.BUILD_NUMBER} finished successfully.")
                        }

                        // 3. Microsoft Teams Notification
                        if (config.teamsWebhookUrl) {
                            def teamsNotifier = new TeamsNotifier(this)
                            teamsNotifier.sendNotification([
                                teamsWebhookUrl: config.teamsWebhookUrl,
                                status: 'SUCCESS',
                                colorHex: '28a745'
                            ], successMsg)
                        }
                    }
                }
            }
        }
    }
}
