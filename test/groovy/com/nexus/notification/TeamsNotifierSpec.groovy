#!/usr/bin/env groovy

package com.nexus.notification

class TeamsNotifier implements Serializable {

    // Reference to the active Jenkins pipeline steps context
    private final def steps

    /**
     * Constructor initializing the notifier with the pipeline script context.
     * @param steps The pipeline script context (usually 'this')
     */
    TeamsNotifier(steps) {
        this.steps = steps
    }

    /**
     * Sends a rich notification card to Microsoft Teams via a webhook URL.
     *
     * @param config Map containing configuration options (teamsWebhookUrl, status, colorHex)
     * @param message The body text of the notification
     */
    void sendNotification(Map config, String message) {
        // Return early if the webhook URL is not defined
        if (!config.teamsWebhookUrl) return

        // Execute the HTTP POST request to the Microsoft Teams webhook endpoint
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
