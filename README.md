# Nexus Jenkins Shared Library

This Jenkins Shared Library provides standardized pipelines, security checks, deployment scripts, and cost optimization utilities for projects at Nexus.

## Directory Structure

* `src/com/nexus/`: Groovy classes providing core logic (e.g., `PipelineConfig`, `SecurityGuard`, `CostOptimizer`).
* `vars/`: Global Jenkins variables and pipeline scripts that can be called directly from a `Jenkinsfile`.
* `resources/`: Static resources and configuration files (e.g., S3 configurations).

## Features

### 1. Standard Pipeline (`standardPipeline.groovy`)

A declarative pipeline wrapper that standardizes the CI process across multiple projects. It includes stages for Initialization, Security Compliance Scanning, Building, and Cloud Cost Optimization.

**Usage in `Jenkinsfile`:**

```groovy
@Library('nexus-shared-lib') _

standardPipeline(
    projectName: 'my-awesome-microservice',
    environment: 'staging',
    runSecurityScan: true,
    optimizeCosts: false
)
```

**Configuration Parameters:**

* `projectName` (String): The name of the project. If not provided, it falls back to the Jenkins job name.
* `environment` (String): Target environment (default: `development`).
* `runSecurityScan` (Boolean): Feature toggle to run the mock secret scanner (default: `true`).
* `optimizeCosts` (Boolean): Feature toggle to run the infrastructure cost optimization analysis (default: `false`).

### 2. S3 Deployment (`deployS3.groovy`)

A helper step to sync a local directory to an AWS S3 bucket. It automatically fetches the appropriate AWS credentials and bucket names based on the target environment using `resources/scripts/configS3.yaml`.

**Usage:**

```groovy
stage('Deploy to S3') {
    steps {
        deployS3('staging', './build-output')
    }
}
```

**Supported Environments:**
Environments and their respective configurations are managed in `resources/scripts/configS3.yaml`. Currently supported:

* `production`
* `staging`

### 3. Security Guard (`SecurityGuard.groovy`)

A utility class that runs mock secret scanning to ensure hardcoded credentials aren't pushed to the repository. If leaked secrets are detected, the pipeline will abort.

### 4. Cost Optimizer (`CostOptimizer.groovy` / `costOptimizeNodes.groovy`)

A utility to analyze Jenkins node clusters and identify idle instances that have been running for too long, mapping out potential cost savings by pruning them.

## Configuration

### S3 Configuration (`configS3.yaml`)

Update `resources/scripts/configS3.yaml` to change bucket names or deployment regions:

```yaml
production:
  bucketName: nexus-production-bucket
  region: eu-central-1
staging:
  bucketName: nexus-staging-bucket
  region: eu-central-1
```

## Contributing

When adding new features:

1. Add pipeline wrappers or standalone steps to the `vars/` folder.
2. Add complex business logic and helper classes to the `src/com/nexus/` folder.
3. Ensure that non-serializable operations in `src/` use `@NonCPS` where necessary (e.g., standard Groovy `for` loops).
