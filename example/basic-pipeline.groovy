@Library('nexus-shared-lib@v0.0.1') _

nexusPipeline([
    projectName: 'simple-service',
    environment: 'dev',
    buildTool: 'gradle'
])
