# Jenkins Setup Guide

To utilize the `nexus-shared-lib` in your Jenkins instance, follow these configuration steps:

## 1. Global Library Configuration

1. Navigate to **Manage Jenkins** -> **Configure System**.
2. Locate the **Global Pipeline Libraries** section.
3. Click **Add** to create a new entry:
   - **Name:** `nexus-shared-lib`
   - **Default version:** `main` (or a specific release tag, e.g., `v1.0.0`).
   - **Retrieval method:** Select **Modern SCM** -> **Git**.
   - **Project Repository:** `https://github.com/your-org/nexus-shared-lib.git`
   - **Credentials:** Provide the appropriate credentials for repository access.
4. Click **Save**.

## 2. Prerequisites

Ensure the following Jenkins plugins are installed and configured:

- **Pipeline: Shared Groovy Libraries**
- **Git Plugin**
- **Docker Pipeline** (if using containerized builds)
- **SonarQube Scanner for Jenkins** (for code quality analysis)
- **Credentials Plugin** (for managing registry and secret tokens)
