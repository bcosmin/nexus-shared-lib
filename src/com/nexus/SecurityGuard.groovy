// src/com/nexus/SecurityGuard.groovy
package com.nexus

import java.io.Serializable

class SecurityGuard implements Serializable {

    // This variable holds the Jenkins pipeline context
    private animateSteps

    // Constructor: Accepts the Jenkins 'steps' object
    SecurityGuard(animateSteps) {
        this.animateSteps = animateSteps
    }

    /**
     * Executes a mock secret scan. In a real implementation, this would run a tool like TruffleHog or GitLeaks.
     * @param repoUrl The repository string to evaluate
     * @return boolean true if safe, false if secrets leaked
     */
    boolean performSecretScan(String repoUrl) {
        // We use 'animateSteps' to execute native Jenkins DSL commands
        animateSteps.echo "Scanning repository: ${repoUrl} for hardcoded credentials..."

        // Groovy optimization: local variable assignment
        def mockScanResultCode = 0 // Simulating a clean bash command return code

        if (mockScanResultCode != 0) {
            animateSteps.error "CRITICAL ERROR: Leaked secrets detected in codebase!"
            return false
        }

        animateSteps.echo "SUCCESS: Codebase passed compliance checks."
        return true
    }
}