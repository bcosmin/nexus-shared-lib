#!/usr/bin/env groovy

package com.nexus

import java.io.Serializable

class Logger implements Serializable {

    private final def steps

    Logger(steps) {
        this.steps = steps
    }

    void info(String message) {
        steps.echo "[INFO] '\u2139\ufe0f' ${message}"
    }


    void warn(String message) {
        steps.echo "[WARN] ⚠️ ${message}"
    }

    void success(String message) {
        steps.echo "[SUCCESS] ✅ ${message}"
        step.currentBuild.result = 'SUCCESS'
    }

    void error(String message) {
        steps.echo "[ERROR] ❌ ${message}"
        step.currentBuild.result = 'FAILURE'
    }
}
