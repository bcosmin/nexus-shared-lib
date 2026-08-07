#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:], Closure criticalBlock) {
    Logger log = new Logger(this)

    def releaseName   = params.releaseName
    def namespace     = params.namespace ?: 'default'
    def rollbackCmd   = params.rollbackCmd ?: (releaseName ? "helm rollback ${releaseName} --namespace ${namespace}" : '')
    def enableRollback = params.enableRollback != null ? params.enableRollback : true

    log.info("Executing critical block with safe rollback protection...")

    try {
        // Execute the critical block of code, which may include deployment or other sensitive operations
        criticalBlock.delegate = delegate
        criticalBlock.resolveStrategy = Closure.DELEGATE_FIRST
        return criticalBlock.call()

    } catch (Exception e) {
        log.error("Critical step failed: ${e.message}")

        if (enableRollback && rollbackCmd) {
            log.warn("Initiating automated safe rollback...")
            try {
                // Executing the rollback command to revert to the previous stable state
                log.info("Executing rollback command: ${rollbackCmd}")
                sh rollbackCmd
                log.info("Rollback completed successfully.")
            } catch (rollbackEx) {
                log.error("CRITICAL: Rollback execution also failed: ${rollbackEx.message}")
            }
        } else {
            log.warn("Rollback was skipped or no rollback command was provided.")
        }

        // Rethrow the original exception to ensure the pipeline fails and notifies stakeholders
        error "Pipeline failed during critical execution: ${e.message}"
    }
}
