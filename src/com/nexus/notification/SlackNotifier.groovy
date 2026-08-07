#!/usr/bin/env groovy

package com.nexus.notification

class SlackNotifier implements Serializable {

    // Reference to the active Jenkins pipeline steps context
    private final def steps

    /**
     * Constructor initializing the Slack notifier with the pipeline script context.
     * @param steps The pipeline script context (usually 'this')
     */
    SlackNotifier(steps) {
        this.steps = steps
    }

    /**
     * Sends a notification message to a specified Slack channel.
     *
     * @param config Map containing configuration options (slackChannel, color)
     * @param message The text content of the notification
     */
    void sendNotification(Map config, String message) {
        // Return early if the Slack channel destination is not defined
        if (!config.slackChannel) return

        // Execute the native Slack plugin step to dispatch the message
        steps.slackSend(
            channel: config.slackChannel,
            color: config.color ?: 'good',
            message: message
        )
    }
}
