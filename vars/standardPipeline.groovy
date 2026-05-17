// vars/standardPipeline.groovy
package com.nexus

import com.nexus.PipelineConfig
import com.nexus.SecurityGuard
import com.nexus.CostOptimizer
import com.nexus.Logger


def call(Map configMap = [:]) {

    String fallbackProjectName = env.JOB_BASE_NAME ?: 'nexus-unknown-fallback'

    def config = new PipelineConfig(configMap, fallbackProjectName)

    if (!config.projectName) {
        Logger.warn(" 'projectName' must be defined in your Jenkinsfile. Using default: ${config.projectName}")
    }

    // Define stages of the pipeline
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
                    Logger.info("Initializing pipeline for Project: ${config.projectName}")
                    Logger.info("Target Deployment Environment: ${config.environment}")
                    // Add initialization steps here (e.g., checkout, setting up environment variables)
                }
            }

            stage('Security Compliance Scan') {
                when {
                    expression { return config.runSecurityScan == true }
                }
                steps {
                    script {
                        Logger.info("Security scan enabled. Starting compliance checks...")
                        def securityGuard = new com.nexus.SecurityGuard(this)
                        boolean isSafe = securityGuard.performSecretScan(config.repoUrl ?: env.GIT_URL ?: "local-repository")
                        if (!isSafe) {
                            Logger.fatal("Pipeline aborted due to security scan failure.")
                        }
                    }
                }
            }
            stage('Build and Artifact') {
                steps {
                    Logger.info("Building application: ${config.projectName}")
                    // Add build steps here (e.g., Maven, Gradle, npm)
                }
            }
            /* stage('Cloud Cost Optimization') {
                when {
                    expression { return config.optimizeCosts == true }
                }
                steps {
                    script {
                        Logger.info("Analyzing cloud footprint and infrastructure cost optimizations...")
                        def costOptimizer = new com.nexus.CostOptimizer(this)
                        // Simulate fetching node metrics (in real case, this would be dynamic)
                        def nodeMetrics = [
                            [instanceId: 'node-1', isIdle: true, runningHours: 5, costPerHour: 0.10],
                            [instanceId: 'node-2', isIdle: false, runningHours: 2, costPerHour: 0.15],
                            [instanceId: 'node-3', isIdle: true, runningHours: 6, costPerHour: 0.20]
                        ]
                        def nodesToPrune = costOptimizer.analyzeClusterNodes(nodeMetrics)
                        Logger.info("Nodes identified for pruning to save costs: ${nodesToPrune.keySet()}")
                    }
                }

            } */
        }
        post {
            always {
                Logger.info("Pipeline execution completed for Project: ${config.projectName}")
            }
        }
    }
}
