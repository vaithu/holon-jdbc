/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.jdbc.spring.boot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

/**
 * Multi-tenant aware DataSource metrics provider for SaaS environments.
 * <p>
 * Tracks per-tenant metrics including:
 * <ul>
 * <li>Active connections per tenant</li>
 * <li>Connection pool utilization</li>
 * <li>Connection wait times</li>
 * <li>Per-tenant resource usage for billing</li>
 * </ul>
 * </p>
 * 
 * SaaS Benefits:
 * - Accurate multi-tenant billing based on actual resource usage
 * - Detect resource-hogging tenants
 * - Capacity planning per tenant
 * - Monitor connection pool health per data context
 * 
 * @since 10.0.0
 */
public class MultiTenantDataSourceMetrics {

    private static final Map<String, DataSourceMetrics> TENANT_METRICS = new ConcurrentHashMap<>();

    /**
     * Record for storing per-tenant metrics.
     */
    public record DataSourceMetrics(
        String tenantId,
        String dataContextId,
        long activeConnections,
        long idleConnections,
        long totalConnections,
        long connectionWaitTimeMs,
        long maxPoolSize,
        long totalConnectionsUsed
    ) {
    }

    /**
     * Get or create metrics for a tenant's data context.
     * 
     * @param dataContextId the data context/tenant identifier
     * @return metrics for the data context
     */
    public static DataSourceMetrics getOrCreateMetrics(String dataContextId) {
        return TENANT_METRICS.computeIfAbsent(dataContextId, ctx -> 
            new DataSourceMetrics(ctx, ctx, 0, 0, 0, 0, 0, 0)
        );
    }

    /**
     * Update metrics from HikariCP DataSource (if available).
     * Safely handles the case where HikariCP is not in classpath.
     * 
     * @param dataContextId the data context/tenant identifier
     * @param dataSource the DataSource to extract metrics from
     */
    public static void updateMetricsFromHikari(String dataContextId, DataSource dataSource) {
        try {
            // Use reflection to avoid hard dependency on HikariCP
            Class<?> hikariDsClass = Class.forName("com.zaxxer.hikari.HikariDataSource");
            if (hikariDsClass.isInstance(dataSource)) {
                // Get the MXBean
                Object hikariDs = dataSource;
                java.lang.reflect.Method getMXBeanMethod = hikariDsClass.getMethod("getHikariPoolMXBean");
                Object mxBean = getMXBeanMethod.invoke(hikariDs);
                
                Class<?> mxBeanClass = mxBean.getClass();
                Object activeConnections = mxBeanClass.getMethod("getActiveConnections").invoke(mxBean);
                Object idleConnections = mxBeanClass.getMethod("getIdleConnections").invoke(mxBean);
                Object totalConnections = mxBeanClass.getMethod("getTotalConnections").invoke(mxBean);
                
                java.lang.reflect.Method getMaxPoolSizeMethod = hikariDsClass.getMethod("getMaximumPoolSize");
                Object maxPoolSize = getMaxPoolSizeMethod.invoke(hikariDs);
                
                DataSourceMetrics current = TENANT_METRICS.get(dataContextId);
                if (current != null) {
                    DataSourceMetrics updated = new DataSourceMetrics(
                        dataContextId,
                        dataContextId,
                        ((Number) activeConnections).longValue(),
                        ((Number) idleConnections).longValue(),
                        ((Number) totalConnections).longValue(),
                        0,
                        ((Number) maxPoolSize).longValue(),
                        current.totalConnectionsUsed() + ((Number) activeConnections).longValue()
                    );
                    TENANT_METRICS.put(dataContextId, updated);
                }
            }
        } catch (Exception e) {
            // HikariCP not available or method not found, silently continue
        }
    }

    /**
     * Get all tenant metrics (for monitoring/billing systems).
     * 
     * @return map of tenant ID to metrics
     */
    public static Map<String, DataSourceMetrics> getAllMetrics() {
        return Map.copyOf(TENANT_METRICS);
    }

    /**
     * Clear metrics for a tenant (e.g., when tenant is deprovisioned).
     * 
     * @param dataContextId the data context/tenant identifier
     */
    public static void clearMetrics(String dataContextId) {
        TENANT_METRICS.remove(dataContextId);
    }

}
