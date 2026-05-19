#!/usr/bin/env groovy

package com.nexus

import java.io.Serializable
import groovy.json.JsonSlurper
import com.cloudbees.groovy.cps.NonCPS

class SecurityGuard implements Serializable {
    
    private final def steps
    private final PipelineConfig config
    
    SecurityGuard(def steps, PipelineConfig config) {
        this.steps = steps
        this.config = config
    }
    
    ScanResult runComplianceScan() {
        def result = new ScanResult()
        
        // 1. RUN TRUFFLEHOG
        steps.echo "[SecurityGuard] Running Secret Scanning via Trufflehog..."
        String truffleCmd = 'docker run --rm -v $(pwd):/pwd trufflesecurity/trufflehog:latest git file:///pwd --only-verified --json > truffle_report.json || true'
        steps.sh(script: truffleCmd)
        
        // 2. RUN TRIVY
        steps.echo "[SecurityGuard] Running Dependency Analysis via Trivy..."
        String trivyCmd = 'docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v $(pwd):/pwd aquasec/trivy:latest fs /pwd --severity CRITICAL,HIGH --format json --output /pwd/trivy_report.json || true'
        steps.sh(script: trivyCmd)
        
        // 3. READ REPORT WORKSPACE PAYLOADS
        String truffleJsonText = steps.readFile(file: 'truffle_report.json').trim()
        String trivyJsonText = steps.readFile(file: 'trivy_report.json').trim()
        
        evaluateReports(result, truffleJsonText, trivyJsonText)
        
        return result
    }
    
    @NonCPS
    private void evaluateReports(ScanResult result, String truffleText, String trivyText) {
        def slurper = new JsonSlurper()
        
        // Parse Trufflehog leaks
        if (truffleText) {
            try {
                truffleText.eachLine { line ->
                    if (line.trim()) {
                        def leak = slurper.parseText(line)
                        String fingerprint = leak.Fingerprint ?: ''
                        
                        // Check if the secret fingerprint is whitelisted
                        if (config.securityWhitelist.contains(fingerprint)) {
                            steps.echo "[SecurityGuard] Whitelisted Secret Leak Skipped: Fingerprint=${fingerprint}"
                            result.whitelistedIssuesCount++
                        } else {
                            steps.echo "[SECURITY RISK] Leaked Secret Found: Source=${leak.SourceID} | Detector=${leak.DetectorName}"
                            result.secretLeaksCount++
                        }
                    }
                }
            } catch (Exception e) {
                steps.echo "[SecurityGuard] Note processing Trufflehog format: ${e.message}"
            }
        }
        
        // Parse Trivy vulnerabilities
        if (trivyText) {
            try {
                def trivyData = slurper.parseText(trivyText)
                
                if (trivyData?.Results) {
                    trivyData.Results.each { target ->
                        if (target?.Vulnerabilities) {
                            target.Vulnerabilities.each { vuln ->
                                String cveId = vuln.VulnerabilityID ?: ''
                                
                                // Check if this specific CVE ID is whitelisted
                                if (config.securityWhitelist.contains(cveId)) {
                                    steps.echo "[SecurityGuard] Whitelisted CVE Skipped: ${cveId}"
                                    result.whitelistedIssuesCount++
                                } else {
                                    if (vuln.Severity == 'CRITICAL') {
                                        result.criticalCvesCount++
                                    } else if (vuln.Severity == 'HIGH') {
                                        result.highCvesCount++
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                steps.echo "[SecurityGuard] Note processing Trivy JSON layout: ${e.message}"
            }
        }
        
        // Render detailed compliance dashboard summary
        steps.echo """
        ==================================================
        SECURITY COMPLIANCE SUMMARY FOR ${config.projectName.toUpperCase()}
        ==================================================
        - Hardcoded Secret Leaks Found:    ${result.secretLeaksCount}
        - Critical Vulnerabilities (CVEs): ${result.criticalCvesCount}
        - High Vulnerabilities (CVEs):     ${result.highCvesCount}
        - Approved Exceptions Bypassed:    ${result.whitelistedIssuesCount}
        ==================================================
        """.stripIndent()
    }
}

class ScanResult implements Serializable {
    int secretLeaksCount = 0
    int criticalCvesCount = 0
    int highCvesCount = 0
    int whitelistedIssuesCount = 0
    
    Boolean hasCriticalIssues() {
        return secretLeaksCount > 0 || criticalCvesCount > 0
    }
}