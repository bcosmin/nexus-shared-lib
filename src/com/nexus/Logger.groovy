#!/usr/bin/env groovy

package com.nexus

import java.io.Serializable

class Logger implements Serializable {
    
    private final def steps
    
    Logger(def steps) {
        this.steps = steps
    }
    
    void info(String message) {
        steps.echo "[INFO] ${message}"
    }
    
    void warn(String message) {
        steps.echo "[WARN] ⚠️ ${message}"
    }
    
    void error(String message) {
        steps.echo "[ERROR] ❌ ${message}"
        // FIXED: Explicitly referencing the global pipeline script context variable
        steps.currentBuild.result = 'FAILURE'
    }
}