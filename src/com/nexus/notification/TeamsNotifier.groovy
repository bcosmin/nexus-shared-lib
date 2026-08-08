#!/usr/bin/env groovy

package com.nexus.notification

class TeamsNotifier implements Serializable {

    private final def steps

    TeamsNotifier(steps) {
        this.steps = steps
    }

    void sendNotification(Map config, String message) {
        // Return early if notifications are explicitly disabled or webhook URL is missing
        if (config.sendTeamsNotification == false || !config.teamsWebhookUrl) {
            return
        }

        try {
            steps.httpRequest(
                url: config.teamsWebhookUrl,
                httpMode: 'POST',
                contentType: 'APPLICATION_JSON',
                validResponseCodes: '200:299',
                requestBody: """{
                    "@type": "MessageCard",
                    "@context": "http://schema.org/extensions",
                    "summary": "Jenkins Build Notification",
                    "themeColor": "${config.colorHex ?: '00FF00'}",
                    "title": "Pipeline Status: ${config.status ?: 'INFO'}",
                    "text": "${message ? message.replace('\n', '<br>') : ''}"
                }"""
            )
        } catch (Exception e) {
            steps.echo "[WARNING] Could not send Microsoft Teams notification: ${e.message}"
        }
    }
}
