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

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;
import com.amazonaws.services.kinesis.connectors.UnmodifiableBuffer;
import com.amazonaws.services.kinesis.connectors.impl.AllPassFilter;
import com.amazonaws.services.kinesis.connectors.impl.BasicMemoryBuffer;
import com.amazonaws.services.kinesis.connectors.interfaces.IBuffer;
import com.amazonaws.services.kinesis.connectors.interfaces.IEmitter;
import com.amazonaws.services.kinesis.connectors.interfaces.IFilter;
import com.amazonaws.services.kinesis.connectors.interfaces.IKinesisConnectorPipeline;
import com.amazonaws.services.kinesis.connectors.interfaces.ITransformerBase;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsSubscriptionTransformer;

/**
 * This sets up the processing pipeline for the KCL application.
 */
public class StdoutPipeline implements IKinesisConnectorPipeline<CloudWatchLogsEvent, String> {

    @Override
    public IEmitter<String> getEmitter(KinesisConnectorConfiguration configuration) {

        // a very basic emitter that prints strings to STDOUT
        return new IEmitter<String>() {

            @Override
            public List<String> emit(UnmodifiableBuffer<String> buffer) throws IOException {
                for (String record : buffer.getRecords()) {
                    System.out.println(record);
                }
                return Collections.emptyList();
            }

            @Override
            public void fail(List<String> records) {}

            @Override
            public void shutdown() {}

        };
    }

    @Override
    public IBuffer<CloudWatchLogsEvent> getBuffer(KinesisConnectorConfiguration configuration) {

        // a very basic in-heap buffer
        return new BasicMemoryBuffer<CloudWatchLogsEvent>(configuration);
    }

    @Override
    public ITransformerBase<CloudWatchLogsEvent, String> getTransformer(KinesisConnectorConfiguration configuration) {

        // transforms records of CloudWatchLogsEvent type to strings
        return new CloudWatchLogsSubscriptionTransformer<String>() {

            @Override
            public String fromClass(CloudWatchLogsEvent logEvent) throws IOException {
                return new Date(logEvent.getTimestamp()).toString() + " - " + logEvent.getMessage();
            }

        };
    }

    @Override
    public IFilter<CloudWatchLogsEvent> getFilter(KinesisConnectorConfiguration configuration) {

        // no filtering
        return new AllPassFilter<CloudWatchLogsEvent>();
    }
}
