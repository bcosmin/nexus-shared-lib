package com.nexus

import java.io.Serializable
import groovy.json.JsonSlurper
import com.cloudbees.groovy.cps.NonCPS

class CostOptimizer implements Serializable {
    
    private final def steps
    private final PipelineConfig config
    
    // Max monthly budget ceilings enforced by the platform team
    private final double DEV_BUDGET_CEILING = 500.0
    private final double PROD_BUDGET_CEILING = 2500.0
    
    CostOptimizer(def steps, PipelineConfig config) {
        this.steps = steps
        this.config = config
    }
    
    /**
     * Triggers Infracost analysis, exports structured JSON reports, and computes financial metrics
     */
    CostResult runCostAnalysis() {
        def result = new CostResult()
        steps.echo "[CostOptimizer] Gathering cloud footprint delta via Infracost..."
        
        // Output JSON directly to workspace file. We use || true to handle verification steps inside Groovy context safely.
        // In your production platform, the INFRACOST_API_KEY would be securely managed by Jenkins credentials
        String infracostCmd = 'docker run --rm -v $(pwd):/code -e INFRACOST_API_KEY=mock-key-portfolio ' +
                             'infracost/infracost:latest breakdown --path /code --format json --out-file /code/infracost_report.json || true'
                             
        steps.sh(script: infracostCmd)
        
        // Load the saved workspace data payload
        String costJsonText = steps.readFile(file: 'infracost_report.json').trim()
        
        if (costJsonText) {
            evaluateCostReport(result, costJsonText)
        } else {
            steps.echo "[WARNING] Infracost report payload was empty. Skipping calculation logic."
        }
        
        return result
    }
    
    /**
     * NonCPS parsing method handles heavy traversal of cloud resource line-items efficiently
     */
    @NonCPS
    private void evaluateCostReport(CostResult result, String jsonText) {
        def slurper = new JsonSlurper()
        
        try {
            def costData = slurper.parseText(jsonText)
            
            // Extract total monthly budget metrics natively matching Infracost JSON naming rules
            if (costData?.totalMonthlyCost) {
                result.projectedMonthlyCost = costData.totalMonthlyCost as Double
            }
            if (costData?.pastTotalMonthlyCost) {
                result.previousMonthlyCost = costData.pastTotalMonthlyCost as Double
                result.costDelta = result.projectedMonthlyCost - result.previousMonthlyCost
            }
            
            // Loop through resource blocks to identify the highest cloud-cost drivers (Top 3 items)
            if (costData?.projects?.breakdown?.resources) {
                def resourceList = []
                costData.projects.breakdown.resources.each { projectResources ->
                    projectResources.each { resource ->
                        if (resource?.monthlyCost) {
                            resourceList.add([name: resource.name, cost: resource.monthlyCost as Double])
                        }
                    }
                }
                // Sort descending based on cost value
                resourceList.sort { a, b -> b.cost <=> a.cost }
                result.topCostDrivers = resourceList.take(3)
            }
            
        } catch (Exception e) {
            steps.echo "[CostOptimizer] Critical failure interpreting cloud cost metric schemas: ${e.message}"
            // Safe fallbacks to prevent breaking pipeline flow on missing local parsing schemas
            return
        }
        
        // Determine the applicable budget barrier based on target runtime environment
        double runningCeiling = (config.environment == 'production') ? PROD_BUDGET_CEILING : DEV_BUDGET_CEILING
        
        if (result.projectedMonthlyCost > runningCeiling) {
            result.budgetExceeded = true
            result.varianceAmount = result.projectedMonthlyCost - runningCeiling
        }
        
        // Output a beautifully framed platform financial architecture scorecard to the console log
        steps.echo """
        ==================================================
        FINANCIAL ARCHITECTURE SCORECARD: ${config.projectName.toUpperCase()}
        ==================================================
        - Active Deployment Environment:   ${config.environment.toUpperCase()}
        - Assigned Environment Budget:    \$${runningCeiling}
        - Total Projected Monthly Cost:    \$${result.projectedMonthlyCost}
        - Delta Over Previous Revision:    \$${result.costDelta >= 0 ? '+' : ''}${result.costDelta}
        ==================================================
        TOP COST DRIVERS IDENTIFIED IN INFRASTRUCTURE:
        """.stripIndent()
        
        result.topCostDrivers.each { item ->
            steps.echo " -> ${item.name}: \$${item.cost}/mo"
        }
        steps.echo "=================================================="
    }
}

/**
 * Serializable data carrier class tracking financial posture health
 */
class CostResult implements Serializable {
    double projectedMonthlyCost = 0.0
    double previousMonthlyCost = 0.0
    double costDelta = 0.0
    Boolean budgetExceeded = false
    double varianceAmount = 0.0
    List topCostDrivers = []
}