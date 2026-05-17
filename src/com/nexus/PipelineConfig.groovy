package com.nexus

import java.io.Serializable

class PipelineConfig implements Serializable {
    // Project Metadata
    String projectName
    String environment = 'development'
    
    // Feature Toggles
    Boolean runSecurityScan = true
    Boolean optimizeCosts = false
    
    // Constructor accepts the raw map AND a fallback name
    PipelineConfig(Map rawConfig, String fallbackName) {
        // Use provided projectName, otherwise fall back to the dynamic default
        this.projectName = rawConfig.projectName ?: fallbackName
        
        if (rawConfig.environment) this.environment = rawConfig.environment
        
        // Explicit null-checks to preserve boolean defaults
        if (rawConfig.containsKey('runSecurityScan')) {
            this.runSecurityScan = rawConfig.runSecurityScan as Boolean
        }
        if (rawConfig.containsKey('optimizeCosts')) {
            this.optimizeCosts = rawConfig.optimizeCosts as Boolean
        }
    }
}