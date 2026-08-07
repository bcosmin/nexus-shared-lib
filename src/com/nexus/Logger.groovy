#!/usr/bin/env groovy

package com.nexus

import java.io.Serializable

class Logger implements Serializable {

    private final def steps

    Logger(steps) {
        this.steps = steps
    }

    void info(String message) {
        steps.echo "[INFO] ℹ️ ${message}"
    }

    void warn(String message) {
        steps.echo "[WARN] ⚠️ ${message}"
        steps.currentBuild.result = 'UNSTABLE'
    }

    void success(String message) {
        steps.echo "[SUCCESS] ✅ ${message}"
    }

    void error(String message) {
        steps.echo "[ERROR] ❌ ${message}"
        steps.currentBuild.result = 'FAILURE'
    }
}
