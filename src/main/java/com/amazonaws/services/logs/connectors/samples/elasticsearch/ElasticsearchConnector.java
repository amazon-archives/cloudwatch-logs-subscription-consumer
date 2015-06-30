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
package com.amazonaws.services.logs.connectors.samples.elasticsearch;

import com.amazonaws.services.kinesis.connectors.KinesisConnectorRecordProcessorFactory;
import com.amazonaws.services.kinesis.connectors.elasticsearch.ElasticsearchObject;
import com.amazonaws.services.logs.connectors.samples.AbstractConnectorExecutor;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;

public class ElasticsearchConnector extends AbstractConnectorExecutor<ElasticsearchObject> {

    private static String CONFIG_FILE = ElasticsearchConnector.class.getSimpleName() + ".properties";

    public ElasticsearchConnector(String configFile) {
        super(configFile);
    }

    @Override
    public KinesisConnectorRecordProcessorFactory<CloudWatchLogsEvent, ElasticsearchObject> getKinesisConnectorRecordProcessorFactory() {
        return new KinesisConnectorRecordProcessorFactory<CloudWatchLogsEvent, ElasticsearchObject>(
                new ElasticsearchPipeline(), getConfig());
    }

    public static void main(String[] args) {
        ElasticsearchConnector executor = new ElasticsearchConnector(CONFIG_FILE);
        executor.run();
    }
}
