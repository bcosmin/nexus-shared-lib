#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {
    Logger log = new Logger(this)

    def bucket        = params.bucket
    def sourcePath    = params.sourcePath
    def targetPath    = params.targetPath ?: ''
    def awsCredential = params.awsCredential ?: 'aws-s3-credentials'
    def region        = params.region ?: 'eu-central-1'

    if (!bucket) {
        log.error('Parameter "bucket" is required for S3 deployment.')
        error 'S3 deployment failed: Missing bucket name.'
    }

    if (!sourcePath) {
        log.error('Parameter "sourcePath" is required for S3 deployment.')
        error 'S3 deployment failed: Missing sourcePath.'
    }

    log.info("Preparing to upload artifacts from [${sourcePath}] to S3 bucket [s3://${bucket}/${targetPath}]...")

    try {
        // Use the s3Upload step provided by the AWS Steps plugin to upload artifacts to S3
        s3Upload(
            file: sourcePath,
            bucket: bucket,
            path: targetPath,
            credentialsId: awsCredential,
            region: region
        )

        log.info("Artifacts successfully uploaded to S3 bucket [${bucket}]!")

    } catch (Exception e) {
        log.error("Failed to upload artifacts to S3: ${e.message}")
        error "S3 deployment failed: ${e.message}"
    }
}
