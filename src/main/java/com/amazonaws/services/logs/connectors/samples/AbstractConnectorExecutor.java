/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.logs.connectors.samples;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;
import com.amazonaws.services.kinesis.connectors.KinesisConnectorExecutorBase;
import com.amazonaws.services.kinesis.metrics.impl.NullMetricsFactory;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;

/**
 * This sample application consumes log events fed to a Kinesis stream through a CloudWatch Logs subscription and prints
 * out the consumed log events to STDOUT.
 */
public abstract class AbstractConnectorExecutor<T> extends KinesisConnectorExecutorBase<CloudWatchLogsEvent, T> {

    private static final Log LOG = LogFactory.getLog(AbstractConnectorExecutor.class);

    private final KinesisConnectorConfiguration config;

    /**
     * Creates a new CloudWatchLogsSubscriptionsExecutor.
     *
     * @param configFile The name of the configuration file to look for on the classpath.
     */
    public AbstractConnectorExecutor(String configFile) {
        InputStream configFileInputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(configFile);

        if (configFileInputStream == null) {
            String msg = "Could not find resource " + configFile + " in the classpath";
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }

        Properties propertiesFile = new Properties();
        Properties mergedProperties = new Properties();
        try {
            propertiesFile.load(configFileInputStream);

            mergedProperties.putAll(propertiesFile);
            mergedProperties.putAll(System.getProperties());

            configFileInputStream.close();

            this.config = new KinesisConnectorConfiguration(mergedProperties, new DefaultAWSCredentialsProviderChain());
        } catch (IOException e) {
            String msg = "Could not load properties file " + configFile + " from classpath";
            LOG.error(msg, e);
            throw new IllegalStateException(msg, e);
        }

        super.initialize(config, new NullMetricsFactory());
    }

    protected KinesisConnectorConfiguration getConfig() {
        return config;
    }
}
