#!/usr/bin/env groovy

// vars/deployS3.groovy
package com.nexus

def call(String environment, String filePath = '.', String credentialsId) {
    def configText = libraryResource 'yaml/configS3.yaml'
    def config = readYaml text: configText
    def envConfig = config[environment]

    if (!envConfig) {
        error "No S3 configuration found for environment: ${environment}"
    }

    withCredentials([aws(credentialsId: credentialsId, accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
        withEnv([
            "AWS_DEFAULT_REGION=${envConfig.region}"
        ]) {
            sh """
                aws s3 sync ${filePath} s3://${envConfig.bucketName}/
            """
        }
    }
}