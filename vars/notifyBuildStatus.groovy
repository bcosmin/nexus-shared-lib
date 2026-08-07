#!/usr/bin/env groovy

package com.nexus

import com.nexus.Logger
import com.nexus.notification.SlackNotifier
import com.nexus.notification.EmailNotifier
import com.nexus.notification.TeamsNotifier

def call(Map config = [:]) {

    Logger log = new Logger(this)

    String status = config.status ?: 'UNKNOWN'
    String customMessage = config.message ?: ''

    List<String> activeChannels = config.channels ?: []

    config.jobName = env.JOB_NAME ?: 'unknown-job'
    config.buildNumber = env.BUILD_NUMBER ?: '0'
    config.buildUrl = env.BUILD_URL ?: '#'
    config.color = getStatusColor(status)
    config.colorHex = getStatusColorHex(status)

    String defaultMessage = "Pipeline finished with status: *${status}* \nJob: `${config.jobName}` (Build #${config.buildNumber}) \nURL: ${config.buildUrl}"
    String finalMessage = customMessage ? "${customMessage}\n${defaultMessage}" : defaultMessage

    switch (status.toUpperCase()) {
        case 'SUCCESS': log.success(finalMessage); break;
        case 'FAILURE': log.error(finalMessage); break;
        default: log.info(finalMessage); break;
    }

    // Slack notification
    if (activeChannels.contains('slack')) {
            try {
                new SlackNotifier(this).send(config, finalMessage)
            } catch (Exception e) {
                log.warn("Failed to send Slack notification: ${e.message}")
            }
    }

    // Microsoft Teams notification
    if (activeChannels.contains('teams')) {
            try {
                new TeamsNotifier(this).send(config, finalMessage)
            } catch (Exception e) {
                log.warn("Failed to send Teams notification: ${e.message}")
            }
    }

    // Email notification
    if (activeChannels.contains('email')) {
            try {
                new EmailNotifier(this).sendNotification(config, finalMessage)
            } catch (Exception e) {
                log.warn("Failed to send Email notification: ${e.message}")
            }
    }
}

private String getStatusColor(String status) {
    status.toUpperCase() == 'SUCCESS' ? 'good' : 'danger'
}

private String getStatusColorHex(String status) {
    status.toUpperCase() == 'SUCCESS' ? '28a745' : 'dc3545'
}
