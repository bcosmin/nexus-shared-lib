#!/usr/bin/env groovy

import com.nexus.Logger

def call(Map params = [:]) {

    Logger log = new Logger(this)

    if (params.recipients == null || params.recipients.isEmpty()) {
        log.error('Please provide at least one recipient email address.')
        error 'Email step failed: Missing recipients.'
    }

    if (params.subject == null || params.subject.isEmpty()) {
        params.subject = "Email from Jenkins - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    }

    if (params.body == null || params.body.isEmpty()) {
        params.body = "This is an automated email from Jenkins.\n\nJob: ${env.JOB_NAME}\nBuild Number: ${env.BUILD_NUMBER}\nBuild URL: ${env.BUILD_URL}\nResults: ${currentBuild.currentResult}"
    }

    def emailBodyContent
    // Default to true if not specified, or if explicitly true
    if (params.useTemplate == null || params.useTemplate) {
        emailBodyContent = emailTemplate(params.body)
    } else {
        emailBodyContent = params.body
    }

    try {
        emailext(
            subject: params.subject,
            body: emailBodyContent,
            to: params.recipients.join(','),
            mimeType: 'text/html'
        )
        log.info("Email sent successfully to: ${params.recipients.join(', ')}")
    } catch (Exception e) {
        log.error("Failed to send email: ${e.message}")
        error "Email step failed: ${e.message}"
    }
}

// Private helper template method (Compiles fine as a local script method)
def emailTemplate(String body) {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Email Notification</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
        .container { background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        h1 { color: #333; }
        p { color: #666; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Email Notification</h1>
        <p>${body}</p>
    </div>
</body>
</html>"""
}
