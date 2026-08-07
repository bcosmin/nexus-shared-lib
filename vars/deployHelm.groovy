#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {
    Logger log = new Logger(this)

    def releaseName   = params.releaseName
    def chartPath     = params.chartPath
    def namespace     = params.namespace ?: 'default'
    def valuesFiles   = params.valuesFiles ?: []
    def setValues     = params.setValues ?: [:]
    def atomic        = params.atomic != null ? params.atomic : true
    def timeout       = params.timeout ?: '10m'
    def wait          = params.wait != null ? params.wait : true

    if (!releaseName) {
        log.error('Parameter "releaseName" is required for Helm deployment.')
        error 'Helm deployment failed: Missing releaseName.'
    }

    if (!chartPath) {
        log.error('Parameter "chartPath" is required for Helm deployment.')
        error 'Helm deployment failed: Missing chartPath.'
    }

    log.info("Deploying Helm release [${releaseName}] to namespace [${namespace}] using chart [${chartPath}]...")

    try {
        // Build the Helm command with the provided parameters
        def helmCmd = "helm upgrade --install ${releaseName} ${chartPath} --namespace ${namespace} --create-namespace"

        // Add the values files if specified
        if (valuesFiles) {
            valuesFiles.each { file ->
                helmCmd += " -f ${file}"
            }
        }

        // Add individual variables set through a map (--set)
        if (setValues) {
            setValues.each { key, value ->
                helmCmd += " --set ${key}=${value}"
            }
        }

        // Advanced options for safety and timeout
        if (atomic) {
            helmCmd += " --atomic"
        }

        if (wait) {
            helmCmd += " --wait"
        }

        if (timeout) {
            helmCmd += " --timeout ${timeout}"
        }

        log.info("Executing Helm command: ${helmCmd}")
        sh helmCmd

        log.info("Helm release [${releaseName}] deployed successfully!")

    } catch (Exception e) {
        log.error("Helm deployment failed: ${e.message}")
        error "Helm deployment step failed: ${e.message}"
    }
}
