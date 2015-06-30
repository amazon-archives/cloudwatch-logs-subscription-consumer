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
package com.amazonaws.services.logs.connectors.samples.stdout;

import com.amazonaws.services.kinesis.connectors.KinesisConnectorRecordProcessorFactory;
import com.amazonaws.services.logs.connectors.samples.AbstractConnectorExecutor;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;

public class StdoutConnector extends AbstractConnectorExecutor<String> {

    private static String CONFIG_FILE = StdoutConnector.class.getSimpleName() + ".properties";

    public StdoutConnector(String configFile) {
        super(configFile);
    }

    @Override
    public KinesisConnectorRecordProcessorFactory<CloudWatchLogsEvent, String> getKinesisConnectorRecordProcessorFactory() {
        return new KinesisConnectorRecordProcessorFactory<CloudWatchLogsEvent, String>(new StdoutPipeline(),
                getConfig());
    }

    public static void main(String[] args) {
        StdoutConnector executor = new StdoutConnector(CONFIG_FILE);
        executor.run();
    }
}
