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
        def slurper = new JsonSlurper()
        
        // 1. RUN TRUFFLEHOG
        steps.echo "[SecurityGuard] Running Secret Scanning via Trufflehog..."
        String truffleCmd = 'docker run --rm -v $(pwd):/pwd trufflesecurity/trufflehog:latest git file:///pwd --only-verified --json > truffle_report.json || true'
        steps.sh(script: truffleCmd)
        
        // 2. RUN TRIVY
        steps.echo "[SecurityGuard] Running Dependency Analysis via Trivy..."
        String trivyCmd = 'docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v $(pwd):/pwd aquasec/trivy:latest fs /pwd --severity CRITICAL,HIGH --format json --output /pwd/trivy_report.json || true'
        steps.sh(script: trivyCmd)
        
        // 3. READ & PRE-PARSE REPORT WORKSPACE PAYLOADS
        String truffleJsonText = steps.readFile(file: 'truffle_report.json').trim()
        String trivyJsonText = steps.readFile(file: 'trivy_report.json').trim()
        
        List<Map> parsedLeaks = []
        if (truffleJsonText) {
            truffleJsonText.eachLine { line ->
                if (line.trim()) {
                    try {
                        parsedLeaks.add(slurper.parseText(line.trim()) as Map)
                    } catch(e) { }
                }
            }
        }
        
        Map parsedTrivy = [:]
        if (trivyJsonText) {
            try {
                parsedTrivy = slurper.parseText(trivyJsonText.trim()) as Map
            } catch(e) { }
        }
        
        // Hand off clean parsed Map objects directly to the compliance evaluator
        evaluateParsedData(result, parsedLeaks, parsedTrivy)
        
        if (result.hasCriticalIssues() || result.highCvesCount > 0) {
            throw new RuntimeException(
                "[COMPLIANCE FAILURE] Security boundaries broken! Found ${result.secretLeaksCount} secrets, ${result.criticalCvesCount} Critical CVEs, and ${result.highCvesCount} High CVEs."
            )
        }
        
        return result
    }
    
    @NonCPS
    void evaluateParsedData(ScanResult result, List<Map> leaks, Map trivyData) {
        // Safe whitelist extraction
        def whitelist = config?.securityWhitelist ? config.securityWhitelist.collect { it.toString().trim() } : []
        
        // Process leaks list
        for (leak in leaks) {
            // Trim and convert key extraction safely to shield against hidden formatting variants
            String fingerprint = (leak?.Fingerprint ?: leak?.fingerprint ?: '').toString().trim()
            if (!fingerprint) continue
            
            if (whitelist.contains(fingerprint)) {
                steps?.echo "[SecurityGuard] Whitelisted Secret Leak Skipped: Fingerprint=${fingerprint}"
                result.whitelistedIssuesCount++
            } else {
                steps?.echo "[SECURITY RISK] Leaked Secret Found!"
                result.secretLeaksCount++
            }
        }
        
        // Process trivy vulnerabilities object structure
        if (trivyData?.Results) {
            trivyData.Results.each { target ->
                if (target?.Vulnerabilities) {
                    target.Vulnerabilities.each { vuln ->
                        String cveId = (vuln?.VulnerabilityID ?: vuln?.vulnerabilityId ?: '').toString().trim()
                        String severity = (vuln?.Severity ?: vuln?.severity ?: '').toString().trim().toUpperCase()
                        if (!cveId) return
                        
                        if (whitelist.contains(cveId)) {
                            steps?.echo "[SecurityGuard] Whitelisted CVE Skipped: ${cveId}"
                            result.whitelistedIssuesCount++
                        } else {
                            if (severity == 'CRITICAL') {
                                result.criticalCvesCount++
                            } else if (severity == 'HIGH') {
                                result.highCvesCount++
                            }
                        }
                    }
                }
            }
        }
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