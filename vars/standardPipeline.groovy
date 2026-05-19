#!/usr/bin/env groovy
// vars/standardPipeline.groovy

import com.nexus.PipelineConfig
import com.nexus.SecurityGuard
import com.nexus.CostOptimizer
import com.nexus.Logger

def call(Map configMap = [:]) {
    // 1. Instantiate the logger inside the active pipeline execution context
    def log = new Logger(this)

    String fallbackProjectName = env.JOB_BASE_NAME ?: 'nexus-unknown-fallback'

    // 2. Hydrate configuration
    def config = new PipelineConfig(configMap, fallbackProjectName)

    // Log tracking warnings gracefully if fallbacks were triggered
    if (config.projectName == 'nexus-unknown-fallback') {
        log.warn(" 'projectName' was not defined in your Jenkinsfile. Using dynamic fallback: ${config.projectName}")
    }

    // 3. Define stages of the pipeline
    pipeline {
        agent any

        options {
            timestamps()
            timeout(time: 1, unit: 'HOURS')
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }

        stages {
            stage('Initialization') {
                steps {
                    log.info("Initializing pipeline for Project: ${config.projectName}")
                    log.info("Target Deployment Environment: ${config.environment}")
                }
            }

            stage('Security Compliance Scan') {
                when {
                    expression { return config.runSecurityScan }
                }
                steps { 
                    script {
                        log.info("Security scan enabled. Starting compliance checks for ${config.projectName}...")

                        def securityGuard = new SecurityGuard(this, config)
                        def scanResults = securityGuard.runComplianceScan()

                        if (scanResults.hasCriticalIssues()) {
                            log.error("Critical security issues found! Failing the pipeline.")
                            error("Pipeline failed due to critical security issues in ${config.projectName}.")
                        } else {
                            log.info("Security scan completed. No critical issues found.")
                        }
                    }
                }
            }
            
            // HYBRID SLOT 1: Before Build Custom Tasks
            stage('Pre-Build Extensions') {
                when {
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
            
            stage('Build') {
                steps {
                    log.info("Building application: ${config.projectName}")
                    // Add build steps here (e.g., Maven, Gradle, npm)
                }
            }
            
            // HYBRID SLOT 2: After Build Custom Tasks
            stage('Post-Build Extensions') {
                when {
                    expression { return config.afterBuild != null }
                }
                steps {
                    script {
                        log.info("Executing user-defined [afterBuild] tasks...")
                        config.afterBuild.delegate = this
                        config.afterBuild()
                    }
                }
            }

            // HYBRID SLOT 3: Before Deploy Custom Tasks
            stage('Pre-Deployment Extensions') {
                when {
                    expression { return config.beforeDeploy != null }
                }
                steps {
                    script {
                        log.info("Executing user-defined [beforeDeploy] tasks...")
                        config.beforeDeploy.delegate = this
                        config.beforeDeploy()
                    }
                }
            }

            stage('Docker Build and Push') {
                when {
                    expression { return config.buildAndPushDocker }
                }
                steps {
                    log.info("Container lifecycle management enabled. Building and pushing Docker image for ${config.projectName}...")
                    buildAndPushDockerImage(
                        registry: config.dockerRegistry,
                        repoName: config.dockerRepoName,
                        credentialsId: config.dockerCredentialsId,
                        tag: "${env.BUILD_NUMBER}"
                    )

                    // Output a standardized platform container metadata scorecard
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
            
            stage('Upload Artifacts to S3') {
                when {
                    expression { return config.uploadArtifacts }
                }
                steps {
                    log.info("Uploading artifacts to S3 for application: ${config.projectName}")
                    uploadToS3(
                        bucket: config.s3Bucket,
                        region: config.awsRegion,
                        credentialsId: config.awsCredentialsId,
                        artifactPattern: 'build/libs/*.jar',
                        targetFolder: "${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}"
                    )
                }
            }
            
            stage('Cloud Cost Optimization') {
                when {
                    expression { return config.optimizeCosts }
                }
                steps {
                    script {
                        log.info("Cost tracking optimization enabled. Scanning infrastructure files...")
                        
                        def costOptimizer = new CostOptimizer(this, config)
                        def costResults = costOptimizer.runCostAnalysis()
                        
                        if (costResults.budgetExceeded) {
                            log.warn("BUDGET ALERT: Infrastructure spend (\$${costResults.projectedMonthlyCost}) exceeds the environment ceiling by \$${costResults.varianceAmount}!")
                        } else {
                            log.info("Cost optimization complete. Projections sit safely within normal parameters.")
                        }
                    }
                }
            }

            // HYBRID SLOT 4: After Deploy Custom Tasks
            stage('Post-Deployment Extensions') {
                when {
                    expression { return config.afterDeploy != null }
                }
                steps {
                    script {
                        log.info("Executing user-defined [afterDeploy] testing blocks...")
                        config.afterDeploy.delegate = this
                        config.afterDeploy()
                    }
                }
            }
        }
        
        post {
            always {
                log.info("Pipeline execution completed for Project: ${config.projectName}")
            }
            failure {
                if (config.sendEmailOnFailure) {
                    sendEmail(
                        recipients: 'myEmail@company.com',
                        subject: "CRITICAL PIPELINE FAILURE: ${config.projectName} [${config.environment.toUpperCase()}]",
                        useTemplate: true
                    )
                }
                log.error("Pipeline failed for Project: ${config.projectName}")
            }
            success {
                if (config.sendEmailOnFailure) {
                    sendEmail(
                        recipients: 'myEmail@company.com',
                        subject: "SUCCESSFUL PIPELINE: ${config.projectName} [${config.environment.toUpperCase()}]",
                        useTemplate: true
                    )
                }
                log.info("Pipeline succeeded for Project: ${config.projectName}")
            }
        }
    }
}