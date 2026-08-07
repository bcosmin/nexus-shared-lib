# Security & Compliance Guard Reference

The `SecurityGuard` component enforces automated compliance audits within the pipeline. When enabled (`runAdvancedSecurityGuard: true`), it scans the project workspace for leaked secrets and high-risk vulnerabilities before allowing the pipeline to proceed.

---

## Included Scanning Engines

1. **Secret Scanning (Trufflehog):**
   - Scans the entire Git repository history and workspace files for exposed secrets, private keys, tokens, and hardcoded credentials.
   - Runs in verified-only mode to minimize false positives.

2. **Vulnerability Analysis (Trivy):**
   - Scans filesystem dependencies and project files for known Common Vulnerabilities and Exposures (CVEs).
   - Filters findings specifically targeting **CRITICAL** and **HIGH** severity levels.

---

## Enforcement & Policy Rules

The build will be **immediately failed** if the security audit discovers:

- Any active secret leaks.
- Any **Critical** severity CVEs.
- Any **High** severity CVEs.

---

## Whitelisting Known Issues

If a detected vulnerability or secret leak is a known false positive or has been temporarily accepted by security management, you can define a whitelist array in your project configuration (if supported) or handle exceptions accordingly.

---

## Usage Example in `Jenkinsfile`

```groovy
@Library('nexus-shared-lib@v1.0.0') _

nexusPipeline([
    projectName: 'secure-microservice',
    environment: 'production',

    // Enable advanced security compliance checks
    runAdvancedSecurityGuard: true,

    buildTool: 'gradle',
    deployToK8s: true,
    helmReleaseName: 'secure-microservice'
])
```
