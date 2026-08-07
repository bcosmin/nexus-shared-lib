# Configuration Parameters Reference

The `nexusPipeline` wrapper is powered by the `PipelineConfig` class. Below is the complete list of configuration options that can be passed as a Map from your project's `Jenkinsfile`.

---

## General Configuration

| Parameter | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `projectName` | String | `env.JOB_BASE_NAME` | Unique name of the project or microservice. |
| `environment` | String | `'development'` | Target deployment environment (e.g., `development`, `staging`, `production`). |
| `branch` | String | `'main'` | Specific Git branch to check out. |
| `gitCredentialsId` | String | `''` | Jenkins credentials ID used for SCM checkout. |

---

## Code Quality & Security

| Parameter | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `runSecurityScan` | Boolean | `false` | Enables static code analysis via SonarQube. |
| `runAdvancedSecurityGuard` | Boolean | `false` | Enables Trufflehog secret scanning and Trivy vulnerability analysis. |
| `buildTool` | String | `'gradle'` | Build stack selector (`'gradle'` or `'maven'`). |
| `sonarAdditionalArgs` | String | `''` | Extra CLI arguments passed to the SonarQube scanner. |

---

## Containerization (Docker)

| Parameter | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `buildAndPushDocker` | Boolean | `false` | Enables building and pushing a container image. |
| `dockerRegistry` | String | `''` | Target container registry domain (e.g., `registry.company.io`). |
| `dockerImageName` | String | `projectName` | Name/repository path of the container image. |
| `dockerfilePath` | String | `'Dockerfile'` | Path to the Dockerfile relative to the workspace root. |
| `dockerCredentialsId` | String | `''` | Jenkins credentials ID for authenticating with the container registry. |

---

## Artifact Distribution (AWS S3)

| Parameter | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `uploadArtifactsToS3` | Boolean | `false` | Enables uploading build artifacts to an AWS S3 bucket. |
| `s3Bucket` | String | `''` | Name of the target AWS S3 bucket. |
| `s3SourcePath` | String | `'build/libs'` | Local workspace path containing the build artifacts. |
| `awsRegion` | String | `'eu-central-1'` | AWS region where the bucket is hosted. |
| `awsCredentialsId` | String | `'aws-s3-credentials'` | Jenkins credentials ID for AWS authentication. |

---

## Artifact Distribution (JFrog Artifactory)

| Parameter | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `uploadToArtifactory` | Boolean | `false` | Enables uploading build artifacts to JFrog Artifactory. |
| `artifactoryServerId` | String | `'artifactory-server'` | Configured Jenkins JFrog server identifier. |
| `artifactoryTargetRepo` | String | `'libs-release-local'` | Target repository name inside Artifactory. |
| `artifactoryArtifactPath` | String | `'build/libs'` | Local file path of artifacts to publish. |

---

## Kubernetes & Helm Deployment

| Parameter | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `deployToK8s` | Boolean | `false` | Enables automated application deployment to Kubernetes using Helm. |
| `helmReleaseName` | String | `projectName` | Unique name of the Helm release. |
| `helmChartPath` | String | `'./charts'` | File path to the Helm chart directory. |
| `helmNamespace` | String | `'default'` | Target Kubernetes namespace for deployment. |
| `helmSetValues` | Map | `[:]` | Dynamic override values passed to the Helm chart. |

---

## Notifications

| Parameter | Type | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `notificationEmail` | String | `''` | Recipient email address for build status alerts. |
| `notifyOnSuccess` | Boolean | `false` | Dispatches success notifications in addition to failure alerts. |
| `slackChannel` | String | `''` | Target Slack channel ID or name (e.g., `#devops-alerts`). |
| `teamsWebhookUrl` | String | `''` | Microsoft Teams incoming Webhook URL for adaptive card alerts. |
