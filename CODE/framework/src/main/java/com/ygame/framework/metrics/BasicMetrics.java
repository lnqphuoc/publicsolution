/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ygame.framework.metrics;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.ygame.framework.common.Config;
import com.ygame.framework.utils.ConvertUtils;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author huynxt
 */
public abstract class BasicMetrics {
    protected final MetricRegistry metrics = new MetricRegistry();
    protected final String group;
    protected final String type;
    protected final String scope;
    protected final String namePrefix;
    protected final boolean haveConfig;
    
    protected BasicMetrics(String instanceName) {
        this.namePrefix = ConvertUtils.toString(Config.getParam(instanceName, "prefix"), "Metrics");
        this.group = ConvertUtils.toString(Config.getParam(instanceName, "group"), "");
        this.type = ConvertUtils.toString(Config.getParam(instanceName, "type"), "");
        this.scope = ConvertUtils.toString(Config.getParam(instanceName, "scope"), "");
        
        if((this.group + this.type + this.scope).length() <= 0) {
            haveConfig = false;
        }
        else {
            haveConfig = true;
        }
        
        if(haveConfig) {
            boolean b = ConvertUtils.toBoolean(Config.getParam(instanceName, "graphite"), false);
            if (b) {
                String graphiteHost = Config.getParam(instanceName, "graphite_host");
                int graphitePort = ConvertUtils.toInt(Config.getParam(instanceName, "graphite_port"), 2003);
                int graphitePeriod = ConvertUtils.toInt(Config.getParam(instanceName, "graphite_period"), 1); // minute
                final Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));
                final GraphiteReporter reporter = GraphiteReporter.forRegistry(metrics).prefixedWith(namePrefix).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).filter(MetricFilter.ALL).build(graphite);
                reporter.start(graphitePeriod, TimeUnit.MINUTES);
            }

            b = ConvertUtils.toBoolean(Config.getParam(instanceName, "jmx_report"), false);
            if (b) {
                final JmxReporter jmxReporter = JmxReporter.forRegistry(metrics).build();
                jmxReporter.start();
            }
        }
    }
    
    protected final String getName(String name) {
        return MetricRegistry.name(group, type, "", scope, name);
    }
}
