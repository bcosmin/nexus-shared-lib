# Define the content for CHANGELOG.md based on the current state of the library

changelog_content = """# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.0.1] - 2026-08-08

### Added

- Initial release of Nexus Shared Pipeline Library.
- Implemented `nexusPipeline` wrapper for standard CI/CD flows.
- Added security scanners (Trufflehog, Trivy) and SonarQube integration.
- Added multi-channel notification support (Slack, Teams, Email).
- Created comprehensive documentation in `docs/` folder, including:
  - Jenkins Setup Guide (`jenkins-setup.md`)
  - Vars Guide (`vars-guide.md`)
  - Best practices for adding new vars (`contributing-vars.md`)
- Implemented Spock unit tests for `nexusPipeline` and associated utilities.

### Fixed

- Resolved CodeNarc violations in `SecurityGuard.groovy`, `notifyBuildStatus.groovy`, `scmCheckout.groovy`, and `sonarqubeScan.groovy`.
- Optimized `sonarqubeScan` with proper token injection and security best practices.
"""
