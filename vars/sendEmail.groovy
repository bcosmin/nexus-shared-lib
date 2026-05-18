#!/usr/bin/env groovy

//vars/sendEmail.groovy
package com.nexus
import com.nexus.Logger

def call (Map params = [:]) {

    if (params.recipients == null || params.recipients.isEmpty()) {
        Logger.error("Please provide at least one recipient email address.")
        return
    }

    if (params.subject == null || params.subject.isEmpty()) {
        params.subject = "Email from Jenkins - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    }

    if (params.body == null || params.body.isEmpty()) {
        params.body = "Job name: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - result: ${currentBuild.currentResult}"
    }

    def emailBodyContent
    // Default to true if not specified, or if explicitly true
    if (params.useTemplate == null || params.useTemplate == true) {
        emailBodyContent = emailTemplate(params.body)
    } else { // params.useTemplate is explicitly false
        emailBodyContent = params.body
    }

    try {
        emailext (
            to: params.recipients,
            subject: params.subject,
            mimeType: 'text/html',
            body: emailBodyContent
        )
        Logger.info("Email sent successfully to ${params.recipients}.")
    } catch (Exception e) {
        Logger.error("Failed to send email: ${e.message}")
    }
}

// email template method
def emailTemplate (String body) {
    def template = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Email Notification</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 20px;
        }
        .container {
            background-color: #fff;
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        h1 {
            color: #333;
        }
        p {
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Email Notification</h1>
        <p>${body}</p>
    </div>
</body>
</html>"""
    return template
}