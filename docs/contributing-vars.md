# How to Add a New Var

To add a new global pipeline utility or wrapper script to the `vars/` directory, follow this standard structure to ensure compatibility with CodeNarc, unit testing, and logging standards.

## 1. Template Structure

Create a new `.groovy` file inside the `vars/` directory (e.g., `vars/myNewUtility.groovy`):

```groovy
#!/usr/bin/env groovy

import com.nexus.Logger

/**
 * Description of what this utility function does.
 *
 * @param params Map containing configuration parameters
 * @return Result of the operation if applicable
 */
def call(Map params = [:]) {
    Logger log = new Logger(this)

    // 1. Extract and validate parameters with default values
    def paramName = params.paramName ?: 'defaultValue'

    log.info("Executing custom utility with parameter: ${paramName}")

    try {
        // 2. Core pipeline logic
        // Example: sh "echo 'Running steps...'"

        log.info("Utility executed successfully.")
    } catch (Exception e) {
        log.error("Utility execution failed: ${e.message}")
        error "Failed in myNewUtility: ${e.message}"
    }
}
```

## Best Practices Checklist

- Use the `call` method: Jenkins looks for the `call` method as the primary execution entry point when a global variable is invoked.
- Accept a `Map` parameter: Always accept `Map params = [:]` to support flexible configuration options.
- Standardized Logging: Initialize `Logger log = new Logger(this)` and use `log.info()`, `log.warn()`, or `log.error()` instead of plain `echo`.
- Error Handling: Wrap core execution steps in `try-catch` blocks and use `error "..."` to fail the pipeline gracefully when necessary.
- CodeNarc Compliance: Ensure your code passes local linter rules (`./gradlew check`) before committing.
