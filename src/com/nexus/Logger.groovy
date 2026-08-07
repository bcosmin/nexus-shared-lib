#!/usr/bin/env groovy

package com.nexus

import java.io.Serializable

class Logger implements Serializable {

    private final def steps

    Logger(steps) {
        this.steps = steps
    }

    void info(String message) {
        steps.echo "[INFO] '\u2139\ufe0f' ${message}.toString()"
    }


    void warn(String message) {
        steps.echo "[WARN] ⚠️ ${message}.toString()"
        step.currentBuild.result = 'WARRNING'
    }

    void success(String message) {
        steps.echo "[SUCCESS] ✅ ${message}.toString()"
        step.currentBuild.result = 'SUCCESS'
    }

    void error(String message) {
        steps.echo "[ERROR] ❌ ${message}.toString()"
        step.currentBuild.result = 'FAILURE'
    }
}
