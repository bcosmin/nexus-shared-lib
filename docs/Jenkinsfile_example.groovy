@Library('nexus-shared-lib@master') _ // Use 'master' or your specific branch name

// This Jenkinsfile demonstrates the full capabilities of the nexusPipeline
// provided by the 'nexus-shared-lib'.
// It enables all feature toggles and provides example configurations
// for S3, Docker, security whitelisting, and custom extension points.

nexusPipeline(
    // --- Project Metadata ---
    projectName: 'my-full-featured-app',
    environment: 'development', // Can be 'development', 'staging', 'production', etc.

    // --- Feature Toggles (set to true to enable all features for this example) ---
    runSecurityScan: true,
    optimizeCosts: true,
    uploadArtifactsToS3: true,
    uploadToArtifactory: true,
    buildAndPushDocker: true,
    sendEmailNotifications: true,

    // --- AWS S3 Configuration (only relevant if uploadArtifactsToS3 is true) ---
    s3Bucket: 'my-full-featured-app-artifacts', // Overrides default 'my-default-bucket'
    awsRegion: 'eu-central-1',                 // Overrides default 'us-east-1'
    awsCredentialsId: 'my-aws-jenkins-creds',  // Jenkins Credential ID for AWS access

    // --- JFrog Artifactory Configuration (only relevant if uploadToArtifactory is true) ---
    artifactoryServerId: 'my-jfrog-server',        // Overrides default 'jfrog-enterprise-server'
    artifactoryTargetRepo: 'my-app-libs-local',    // Overrides default 'generic-local'
    artifactoryCredentialsId: 'my-jfrog-creds',    // Jenkins Credential ID for Artifactory access
    artifactoryPattern: 'build/libs/*.jar',        // Pattern for files to upload (Overrides default '**/*')

    // --- Docker Configuration (only relevant if buildAndPushDocker is true) ---
    dockerRegistry: 'my.private.docker.registry.com', // Example private registry
    dockerCredentialsId: 'my-docker-jenkins-creds',   // Jenkins Credential ID for Docker registry login
    dockerImageName: 'my-full-featured-app-image',    // Custom image name


    // --- Security Whitelist Configuration ---
    // List of CVE IDs or secret hashes to be ignored by the SecurityGuard
    securityWhitelist: [
        'CVE-2023-12345', // Example whitelisted CVE
        'sh-abcdef1234567890' // Example whitelisted secret hash
    ],

    // --- Extension Points (Closures for custom logic at various pipeline stages) ---
    // These closures are executed within the pipeline context, allowing access to 'sh', 'echo', etc.

    beforeBuild: {
        echo "--> Custom step: Running pre-build checks and installing dependencies..."
        // Example: Install Node.js dependencies
        // sh 'npm install'
        // Example: Run unit tests
        // sh 'npm test'
    },

    afterBuild: {
        echo "--> Custom step: Performing post-build artifact verification..."
        // Example: Check build output for expected files
        // sh 'ls -l target/*.jar'
    },

    beforeDeploy: {
        echo "--> Custom step: Preparing deployment environment and running infrastructure checks..."
        // Example: Terraform plan
        // sh 'terraform plan -out=tfplan'
    },

    afterDeploy: {
        echo "--> Custom step: Executing post-deployment integration and smoke tests..."
        // Example: Hit a health endpoint
        // sh 'curl -f http://my-full-featured-app.example.com/health || error "Application health check failed!"'
        // Example: Clean up temporary deployment files
        // sh 'rm -rf /tmp/deployment-artifacts'
    }
)