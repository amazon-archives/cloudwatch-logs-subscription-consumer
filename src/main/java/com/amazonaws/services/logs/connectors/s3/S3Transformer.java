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
package com.amazonaws.services.logs.connectors.s3;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsSubscriptionTransformer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Transforms CloudWatchLogsEvent objects to JSON.
 */
public class S3Transformer extends CloudWatchLogsSubscriptionTransformer<byte[]> {

    private static final Log LOG = LogFactory.getLog(S3Transformer.class);

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    static {
        JSON_OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
    }

    @Override
    public byte[] fromClass(CloudWatchLogsEvent record) throws IOException {
        try {
            return JSON_OBJECT_MAPPER.writeValueAsString(record).getBytes();
        } catch (JsonProcessingException e) {
            String message = "Error serializing the record to JSON: " + e.getMessage();
            LOG.error(message);
            throw new IOException(message, e);
        }
    }
}
