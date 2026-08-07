#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {
    Logger log = new Logger(this)

    def bucket        = params.bucket
    def file          = params.file
    def targetPath    = params.targetPath ?: ''
    def awsCredential = params.awsCredential ?: 'aws-s3-credentials'
    def region        = params.region ?: 'eu-central-1'
    def includePath   = params.includePath ?: ''
    def excludePath   = params.excludePath ?: ''

    if (!bucket) {
        log.error('Parameter "bucket" is required for S3 upload.')
        error 'S3 upload failed: Missing bucket name.'
    }

    if (!file) {
        log.error('Parameter "file" (or source pattern) is required for S3 upload.')
        error 'S3 upload failed: Missing file path or pattern.'
    }

    log.info("Uploading file(s) [${file}] to S3 bucket [s3://${bucket}/${targetPath}]...")

    try {
        def uploadConfig = [
            file: file,
            bucket: bucket,
            path: targetPath,
            credentialsId: awsCredential,
            region: region
        ]

        if (includePath) {
            uploadConfig.includePath = includePath
        }

        if (excludePath) {
            uploadConfig.excludePath = excludePath
        }

        // Execute the upload to S3 using the s3Upload step provided by the AWS S3 Jenkins Plugin
        s3Upload(uploadConfig)

        log.info("File(s) successfully uploaded to S3!")

    } catch (Exception e) {
        log.error("Failed to upload file(s) to S3: ${e.message}")
        error "S3 upload failed: ${e.message}"
    }
}
