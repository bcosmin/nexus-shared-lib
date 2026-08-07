#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {
    Logger log = new Logger(this)

    // Parameters
    def branch       = params.branch ?: env.BRANCH_NAME ?: 'main'
    def credentialsId = params.credentialsId ?: ''
    def shallow      = params.shallow != null ? params.shallow : true
    def depth        = params.depth ?: 1
    def poll         = params.poll != null ? params.poll : true
    def changelog    = params.changelog != null ? params.changelog : true

    log.info("Checking out source code for branch: ${branch} (Shallow: ${shallow}, Depth: ${depth}, Poll: ${poll})")

    try {
        // Jenkins SCM configuration
        def scmConfig = [
            $class: 'GitSCM',
            branches: [[name: "${branch}"]],
            doNotFingerprintGroovy: false,
            poll: poll,
            changelog: changelog,
            extensions: [
                [$class: 'CleanBeforeCheckout'],
                [$class: 'LocalBranch', localBranch: "**"]
            ],
            submoduleCfg: [],
            userRemoteConfigs: []
        ]

        // Add shallow clone if enabled
        if (shallow) {
            scmConfig.extensions << [$class: 'CloneOption', honorRefspec: true, noTags: false, reference: '', shallow: true, depth: depth]
        }

        // Set credentials if provided
        if (credentialsId) {
            scmConfig.userRemoteConfigs << [
                credentialsId: credentialsId,
                url: params.url ?: env.GIT_URL ?: ''
            ]
        } else {
            // If no credentials are specified explicitly, use the current job's URL
            scmConfig.userRemoteConfigs << [
                url: params.url ?: env.GIT_URL ?: ''
            ]
        }

        // Execute the native checkout in Jenkins
        def checkoutResult = checkout(scmConfig)

        log.info("Successfully checked out commit: ${checkoutResult.GIT_COMMIT?.take(7)}")
        return checkoutResult

    } catch (Exception e) {
        log.error("Failed to check out source code: ${e.message}")
        error "SCM Checkout failed: ${e.message}"
    }
}
