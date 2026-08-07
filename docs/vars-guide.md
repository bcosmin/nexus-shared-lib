# Vars Guide (Utility Methods)

This library exposes several global functions in the `vars/` directory that can be invoked directly within any `Jenkinsfile` without additional imports.

| Function | Description | Typical Usage |
| :--- | :--- | :--- |
| `nexusPipeline` | The main orchestrator wrapper for the CI/CD lifecycle. | `nexusPipeline([...])` |
| `scmCheckout` | Simplifies Git SCM checkout with automated shallow cloning and credentials. | `scmCheckout(branch: 'main')` |
| `sonarqubeScan` | Executes static code analysis and enforces Quality Gate checks. | `sonarqubeScan(buildTool: 'gradle')` |
| `notifyBuildStatus` | Sends status notifications via configured channels (Slack, Teams, Email). | `notifyBuildStatus(status: 'SUCCESS')` |

## Individual Method Usage

You are not required to use the full `nexusPipeline` wrapper. You can call utility methods individually for granular control:

```groovy
stage('Security Scan') {
    steps {
        sonarqubeScan(
            buildTool: 'gradle',
            timeoutMinutes: 15,
            additionalArguments: '-Dsonar.projectKey=my-project'
        )
    }
}
```
