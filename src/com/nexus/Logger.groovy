#!/usr/bin/env groovy

package com.nexus

class Logger {
    static void info(String message) {
        echo "INFO: ${message}"
    }

    static void warn(String message) {
        echo "WARNING: ${message}"
    }

    static void error(String message) {
        echo "ERROR: ${message}"
        currentBuild.result = 'FAILURE'
    }

    static void fatal(String message) {
        echo "FATAL: ${message}"
        currentBuild.result = 'FAILURE'
    }

    static void debug(String message) {
        echo "DEBUG: ${message}"
    }

    static void trace(String message) {
        echo "TRACE: ${message}"
    }
}