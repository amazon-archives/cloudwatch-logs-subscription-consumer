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
package com.amazonaws.services.logs.connectors.samples.s3;

import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;
import com.amazonaws.services.kinesis.connectors.impl.AllPassFilter;
import com.amazonaws.services.kinesis.connectors.impl.BasicMemoryBuffer;
import com.amazonaws.services.kinesis.connectors.interfaces.IBuffer;
import com.amazonaws.services.kinesis.connectors.interfaces.IEmitter;
import com.amazonaws.services.kinesis.connectors.interfaces.IFilter;
import com.amazonaws.services.kinesis.connectors.interfaces.IKinesisConnectorPipeline;
import com.amazonaws.services.kinesis.connectors.interfaces.ITransformerBase;
import com.amazonaws.services.kinesis.connectors.s3.S3Emitter;
import com.amazonaws.services.logs.connectors.s3.S3Transformer;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;

/**
 * This sets up the processing pipeline for the KCL application.
 */
public class S3Pipeline implements IKinesisConnectorPipeline<CloudWatchLogsEvent, byte[]> {

    @Override
    public IEmitter<byte[]> getEmitter(KinesisConnectorConfiguration configuration) {

        return new S3Emitter(configuration);
    }

    @Override
    public IBuffer<CloudWatchLogsEvent> getBuffer(KinesisConnectorConfiguration configuration) {

        // a very basic in-heap buffer
        return new BasicMemoryBuffer<CloudWatchLogsEvent>(configuration);
    }

    @Override
    public ITransformerBase<CloudWatchLogsEvent, byte[]> getTransformer(KinesisConnectorConfiguration configuration) {

        return new S3Transformer();
    }

    @Override
    public IFilter<CloudWatchLogsEvent> getFilter(KinesisConnectorConfiguration configuration) {

        // no filtering
        return new AllPassFilter<CloudWatchLogsEvent>();
    }
}
