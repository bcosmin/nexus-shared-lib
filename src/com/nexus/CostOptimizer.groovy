// src/com/nexus/CostOptimizer.groovy
package com.nexus

import java.io.Serializable

class CostOptimizer implements Serializable {
    private def steps

    CostOptimizer(steps) {
        this.steps = steps
    }

    /**
     * NonCPS method because it handles raw data filtering loops.
     * Tells Jenkins: Execute this completely in standard Java memory without trying to pause it.
     */
    @NonCPS
    Map analyzeClusterNodes(List<Map> nodeMetrics) {
        steps.echo "Analyzing cluster matrix for cost-saving opportunities..."
        def targetNodesToPrune = [:]

        // A standard Java loop that runs perfectly fine inside @NonCPS
        for (node in nodeMetrics) {
            if (node.isIdle && node.runningHours > 4) {
                targetNodesToPrune[node.instanceId] = node.costPerHour
            }
        }

        return targetNodesToPrune
    }
}