#!/usr/bin/env groovy

package com.nexus.notification

class EmailNotifier implements Serializable {

    // Reference to the active Jenkins pipeline steps context
    private final def steps

    /**
     * Constructor initializing the Email notifier with the pipeline script context.
     * @param steps The pipeline script context (usually 'this')
     */
    EmailNotifier(steps) {
        this.steps = steps
    }

    /**
     * Sends an email notification using the Jenkins Email Extension plugin.
     *
     * @param config Map containing configuration options (emailRecipients, emailTo, jobName, buildNumber, status)
     * @param message The body content of the email
     */
    void sendNotification(Map config, String message) {
        // Determine recipients from emailRecipients or fallback to emailTo
        def recipients = config.emailRecipients ?: config.emailTo

        // Return early if no recipients are defined
        if (!recipients) return

        // Execute the native Email Extension step to send the email
        steps.emailext(
            to: recipients,
            subject: "[Jenkins] ${config.jobName ?: 'Build'} - Build #${config.buildNumber ?: 'N/A'} (${config.status ?: 'INFO'})",
            body: message
        )
    }
}
