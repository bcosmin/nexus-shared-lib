#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:], Closure body) {
    Logger log = new Logger(this)

    def vaultSecrets = params.secrets ?: []
    def vaultUrl     = params.vaultUrl ?: ''
    def credentialId = params.credentialId ?: 'vault-approle-credentials'

    if (vaultSecrets.isEmpty()) {
        log.error('Parameter "secrets" is required for Vault integration.')
        error 'Vault integration failed: Missing secrets configuration.'
    }

    log.info("Configuring HashiCorp Vault session to fetch secrets...")

    try {
        // Mapping the Vault configuration parameters to the withVault step
        def vaultConfig = [
            vaultSecrets: vaultSecrets
        ]

        if (vaultUrl) {
            vaultConfig.vaultUrl = vaultUrl
        }

        if (credentialId) {
            vaultConfig.credentialId = credentialId
        }

        // Using the withVault step to establish a session and retrieve secrets
        withVault(vaultConfig) {
            log.info("Vault session established successfully. Executing wrapped block...")
            // Setting the delegate and resolve strategy for the closure to ensure proper variable resolution
            body.delegate = delegate
            body.resolveStrategy = Closure.DELEGATE_FIRST
            return body.call()
        }

    } catch (Exception e) {
        log.error("Failed to retrieve secrets from Vault: ${e.message}")
        error "Vault step failed: ${e.message}"
    }
}
