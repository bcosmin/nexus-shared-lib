@Library('nexus-shared-lib@v0.0.1') _

nexusPipeline([
    projectName: 'enterprise-app',
    environment: 'production',
    buildTool: 'maven',

    // Security & Quality
    runSecurityScan: true,
    runAdvancedSecurityGuard: true,

    // Docker
    buildAndPushDocker: true,
    dockerRegistry: 'registry.company.io',
    dockerImageName: 'nexus/enterprise-app',

    // Deployment
    deployToK8s: true,
    helmChartPath: './charts/enterprise-app',

    // Notifications
    slackChannel: '#team-backend',
    notificationEmail: 'team@company.com'
])
