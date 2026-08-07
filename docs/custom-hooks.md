# Custom Extension Hooks Reference

The `nexusPipeline` wrapper provides dedicated extension slots (hooks) that allow teams to inject custom Groovy closures directly into specific stages of the pipeline without modifying the core library code.

---

## Available Extension Hooks

| Hook Parameter | Execution Timing | Description |
| :--- | :--- | :--- |
| `beforeBuild` | Right before the compilation stage | Ideal for downloading configuration files, setting up local environment variables, or running database migration checks. |
| `afterBuild` | Right after the application build succeeds | Ideal for running custom unit/integration tests or packaging auxiliary assets. |
| `beforeDeploy` | Prior to container distribution or deployment | Ideal for performing validation checks or scanning pre-requisites. |
| `afterDeploy` | At the conclusion of the pipeline workflow | Ideal for running smoke tests, notifying external systems, or triggering downstream jobs. |

---

## Usage Example in `Jenkinsfile`

You can define any standard pipeline steps inside these closures. They have full access to the active pipeline context (`this`).

```groovy
@Library('nexus-shared-lib@v1.0.0') _

nexusPipeline([
    projectName: 'payment-service',
    environment: 'production',
    buildTool: 'gradle',

    // Custom pre-build routine
    beforeBuild: {
        echo "Executing custom database schema validation..."
        sh './gradlew flywayValidate'
    },

    // Custom post-deployment verification
    afterDeploy: {
        echo "Running post-deployment health check smoke tests..."
        sh 'curl --fail [https://api.company.io/healthz](https://api.company.io/healthz)'
    }
])
```
