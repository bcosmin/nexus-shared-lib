// vars/deployS3.groovy
package com.nexus

def call(String environment, String filePath = '.') {
    def configText = libraryResource 'scripts/configS3.yaml'
    def config = readYaml text: configText
    def envConfig = config[environment]

    if (!envConfig) {
        error "No S3 configuration found for environment: ${environment}"
    }

    withEnv([
        "AWS_ACCESS_KEY_ID=${envConfig.accessKeyId}",
        "AWS_SECRET_ACCESS_KEY=${envConfig.secretAccessKey}",
        "AWS_DEFAULT_REGION=${envConfig.region}"
    ]) {
        sh """
            aws s3 sync ${filePath} s3://${envConfig.bucketName}/
        """
    }
}