#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {
    Logger log = new Logger(this)

    def scannerInstallation = params.scannerInstallation ?: 'SonarQubeScanner'
    def credentialsId       = params.credentialsId ?: 'sonar-token'
    def additionalArguments = params.additionalArguments ?: ''
    def waitForQualityGate  = params.waitForQualityGate != null ? params.waitForQualityGate : true
    def timeoutMinutes      = params.timeoutMinutes ?: 10

    log.info("Starting SonarQube code analysis using credentials: ${credentialsId}...")

    try {
        // Wrap execution with credentials to inject the Sonar token properly
        withCredentials([string(credentialsId: credentialsId, variable: 'SONAR_TOKEN')]) {
            // Set up SonarQube environment with the specified scanner installation
            withSonarQubeEnv(scannerInstallation) {

                // Determining the scanner command based on the build tool specified in params
                def scannerCommand = params.buildTool == 'maven' ? "mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN}" :
                                     params.buildTool == 'gradle' ? "./gradlew sonarqube -Dsonar.token=${SONAR_TOKEN}" :
                                     "sonar-scanner -Dsonar.token=${SONAR_TOKEN}"

                // Adding additional arguments if they exist (e.g., project keys, sources, etc.)
                if (additionalArguments) {
                    scannerCommand += " ${additionalArguments}"
                }

                log.info("Executing scanner command with token injection")

                // Executing the SonarQube scanner command
                sh scannerCommand
            }
        }

        // If waitForQualityGate is true, wait for the Quality Gate result
        if (waitForQualityGate) {
            log.info("Waiting for SonarQube Quality Gate result (Timeout: ${timeoutMinutes} minutes)...")

            timeout(time: timeoutMinutes, unit: 'MINUTES') {
                def qg = waitForQualityGate()
                if (qg.status != 'OK') {
                    log.error("Pipeline failed: SonarQube Quality Gate status is [${qg.status}]")
                    error "SonarQube Quality Gate failed with status: ${qg.status}"
                }
                log.info("SonarQube Quality Gate passed successfully!")
            }
        }

    } catch (Exception e) {
        log.error("SonarQube scan failed: ${e.message}")
        error "SonarQube step failed: ${e.message}"
    }
}
