# Nexus Jenkins Shared Library

This Jenkins Shared Library provides standardized pipelines, security checks, deployment scripts, and cost optimization utilities for projects at Nexus.

## Directory Structure

* `src/com/nexus/`: Groovy classes providing core logic (e.g., `PipelineConfig`, `SecurityGuard`, `CostOptimizer`).
* `vars/`: Global Jenkins variables and pipeline scripts that can be called directly from a `Jenkinsfile`.
* `resources/`: Static resources and configuration files (e.g., S3 configurations).

## Features

### 1. Standard Pipeline (`nexusPipeline.groovy`)

A declarative pipeline wrapper that standardizes the CI process across multiple projects. It includes stages for Initialization, Security Compliance Scanning, Building, and Cloud Cost Optimization.

**Usage in `Jenkinsfile`:**

```groovy
@Library('nexus-shared-lib') _

nexusPipeline(
    projectName: 'my-awesome-microservice',
    environment: 'staging',
    runSecurityScan: true,
    optimizeCosts: false
)
```

**Configuration Parameters:**

*   `projectName` (String): The name of the project. If not provided, it falls back to the Jenkins job name (`env.JOB_BASE_NAME`).
*   `environment` (String): Target deployment environment (e.g., `'development'`, `'staging'`, `'production'`). Default: `'development'`.

*   **Feature Toggles:**
    *   `runSecurityScan` (Boolean): Enables/disables the security compliance scan. Default: `true`.
    *   `optimizeCosts` (Boolean): Enables/disables the cloud cost optimization analysis. Default: `false`.
    *   `uploadArtifactsToS3` (Boolean): Enables/disables artifact upload to AWS S3. Default: `false`.
    *   `buildAndPushDocker` (Boolean): Enables/disables Docker image build and push to a registry. Default: `false`.
    *   `uploadToArtifactory` (Boolean): Enables/disables artifact upload to JFrog Artifactory. Default: `false`.
    *   `sendEmailNotifications` (Boolean): Enables/disables email notifications on pipeline success/failure. Default: `false`.

*   **AWS S3 Configuration** (only relevant if `uploadArtifactsToS3` is `true`):
    *   `s3Bucket` (String): The target S3 bucket name.
    *   `awsRegion` (String): The AWS region for the S3 bucket.
    *   `awsCredentialsId` (String): Jenkins Credential ID for AWS access.

*   **Docker Configuration** (only relevant if `buildAndPushDocker` is `true`):
    *   `dockerRegistry` (String): The URL of the Docker registry (e.g., `'my.private.docker.registry.com'`).
    *   `dockerCredentialsId` (String): Jenkins Credential ID for Docker registry login.
    *   `dockerRepoName` (String): The target repository name for the image.

*   **JFrog Artifactory Configuration** (only relevant if `uploadToArtifactory` is `true`):
    *   `artifactoryServerId` (String): The Artifactory server ID configured in Jenkins.
    *   `artifactoryTargetRepo` (String): The target repository in Artifactory.
    *   `artifactoryCredentialsId` (String): Jenkins Credential ID for Artifactory access.
    *   `artifactoryPattern` (String): A file pattern for artifacts to upload.

*   **Security Whitelist Configuration:**
    *   `securityWhitelist` (List<String>): A list of CVE IDs or secret hashes to be ignored by the `SecurityGuard`.

*   **Extension Points** (Closures for custom logic at various pipeline stages):
    *   `beforeBuild` (Closure): Executed before the main 'Build' stage.
    *   `afterBuild` (Closure): Executed after the main 'Build' stage.
    *   `beforeDeploy` (Closure): Executed before the 'Docker Build and Push' or 'Upload Artifacts' stages.
    *   `afterDeploy` (Closure): Executed after all deployment-related stages.

### 2. S3 Deployment (`deployArtifactsS3.groovy`)

A helper step to sync a local directory to an AWS S3 bucket.

**Usage:**

```groovy
stage('Deploy to S3') {
    steps {
        deployArtifactsS3('my-s3-bucket', 'eu-central-1', './build-output', 'my-aws-credentials-id')
    }
}
```

### 3. Security Guard (`SecurityGuard.groovy`)

A utility class that runs mock secret scanning to ensure hardcoded credentials aren't pushed to the repository. If leaked secrets are detected, the pipeline will abort.

### 4. Cost Optimizer (`CostOptimizer.groovy` / `costOptimizeNodes.groovy`)

A utility to analyze Jenkins node clusters and identify idle instances that have been running for too long, mapping out potential cost savings by pruning them.

## Configuration

### 5. Email Notifications (`sendEmail.groovy`)

A utility step to send email notifications from the pipeline. It supports custom subjects, bodies, and an HTML template for consistent formatting.

**Usage:**

```groovy
stage('Send Notification') {
    steps {
        sendEmail(
            recipients: 'dev-team@example.com, qa-team@example.com',
            subject: "Build ${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
            body: "The build for ${env.JOB_NAME} completed with status: ${currentBuild.currentResult}.",
            useTemplate: true // Set to false to send plain text body
        )
    }
}
```

## Contributing

When adding new features:

1. Add pipeline wrappers or standalone steps to the `vars/` folder.
2. Add complex business logic and helper classes to the `src/com/nexus/` folder.
3. Ensure that non-serializable operations in `src/` use `@NonCPS` where necessary (e.g., standard Groovy `for` loops).
