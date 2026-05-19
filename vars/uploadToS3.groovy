#!/usr/bin/env groovy
import com.nexus.Logger

def call(Map params = [:]) {
    
    def bucket    = params.bucket
    def region    = params.region ?: 'us-east-1'
    def credsId   = params.credentialsId ?: 'aws-devops-portfolio-token' // Step fallback
    def artifact  = params.artifactPattern ?: 'build/libs/*.jar'
    def targetDir = params.targetFolder ?: "${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}"

    if (!bucket) {
        Logger.error('S3 Upload failed: Target bucket parameter is missing.')
        error 'S3 Step Misconfigured'
    }

    Logger.info("Preparing AWS S3 archival context using Jenkins Credential ID [${credsId}]")

    // Enforce secure AWS Credential scoping dynamically
    withAWS(credentials: credsId, region: region) {
        try {
            Logger.info("Uploading artifacts matching [${artifact}] to S3 path: s3://${bucket}/${targetDir}/")
            
            s3Upload(
                file: artifact,
                bucket: bucket,
                path: "${targetDir}/",
                metadatas: ["Project:${env.JOB_BASE_NAME}", "BuildNumber:${env.BUILD_NUMBER}"]
            )
            
            Logger.info('S3 Artifact archival completed successfully.')
        } catch (Exception e) {
            Logger.error("AWS S3 Plugin runtime execution failure: ${e.message}")
            error "Pipeline halted: Unable to secure build artifacts inside S3. Reason: ${e.message}"
        }
    }
}