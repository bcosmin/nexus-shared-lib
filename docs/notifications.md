# Nexus Shared Library

## Notifications

    notifyBuild(
            status: 'SUCCESS',
            channels: ['teams', 'email'], // <-- Aici specifici exact ce vrei să fie activ
            message: 'Build-ul a trecut cu succes pe mediul de UAT.',
            teamsWebhookUrl: 'https://outlook.office.com/webhook/...',
            emailTo: 'myemail@example.com'
        )

## Use Nexus shared library

@Library('nexus-shared-lib@main') _

    nexusPipeline([
        projectName: 'payment-service',
        environment: 'production',
        runSecurityScan: true,
        buildTool: 'gradle',
        buildAndPushDocker: true,
        dockerRegistry: 'my-registry.io',
        dockerImageName: 'nexus/payment-service',
        deployToK8s: true,
        helmReleaseName: 'payment-service',
        helmChartPath: './charts/payment-service',
        helmSetValues: ['image.tag': env.BUILD_NUMBER],
        notificationEmail: 'devops-alerts@company.com'
    ])
