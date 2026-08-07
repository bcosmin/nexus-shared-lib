#!/usr/bin/env groovy

package com.nexus.notification

class TeamsNotifier {

    private final def steps

    TeamsNotifier(steps) {
        this.steps = steps
    }

    void sendNotification(Map config, String message) {
        if (!config.teamsWebhookUrl) return

        steps.httpRequest(
            url: config.teamsWebhookUrl,
            httpMode: 'POST',
            contentType: 'APPLICATION_JSON',
            requestBody: """{
                            "@type": "MessageCard",
                            "@context": "http://schema.org/extensions",
                            "summary": "Jenkins Build Notification",
                            "themeColor": "${config.colorHex ?: '00FF00'}",
                            "title": "Pipeline Status: ${config.status}",
                            "text": "${message.replace('\n', '<br>')}"
                        }"""
        )
    }
}
