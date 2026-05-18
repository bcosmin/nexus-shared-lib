# Architecture Diagram

```text
+-------------------------------------------------------------+
|                     App Repository                          |
|  (Includes simple Jenkinsfile calling standardPipeline())   |
+------------------------------+------------------------------+
                               |
                               v Triggers
+-------------------------------------------------------------+
|                    Jenkins Controller                       |
|   (Loads global 'nexus-shared-lib' dynamically from Git)    |
+------------------------------+------------------------------+
                               |
                               v Executes Steps
+-------------------------------------------------------------+
|                     nexus-shared-lib                        |
|                                                             |
|  +-------------------+  +----------------+  +------------+  |
|  | Enforce Security  |  | Docker Build & |  | Cloud Cost |  |
|  |  (Trufflehog/Trivy)| | Push to ECR    |  | Optimizer  |  |
|  +-------------------+  +----------------+  +-----+------+  |
+---------------------------------------------------|---------+
                                                    |
                                                    v AWS API Call
                                      +-------------+-------------+
                                      |    AWS Ec2 / Auto-Scaler  |
                                      |  (Terminates idle nodes)  |
                                      +---------------------------+
```

## Notes

- The diagram illustrates the flow from the app repository through Jenkins to the shared library.
- `nexus-shared-lib` contains security enforcement, Docker build/push, and cost optimization.
- The cost optimizer interacts with AWS to terminate idle EC2 nodes.
