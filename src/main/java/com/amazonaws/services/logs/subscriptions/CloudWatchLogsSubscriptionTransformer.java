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
package com.amazonaws.services.logs.subscriptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.kinesis.connectors.interfaces.ICollectionTransformer;
import com.amazonaws.services.kinesis.model.Record;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts a Kinesis record to a collection of CloudWatchLogsEvent records.
 */
public abstract class CloudWatchLogsSubscriptionTransformer<T> implements
        ICollectionTransformer<CloudWatchLogsEvent, T> {

    private static final Log LOG = LogFactory.getLog(CloudWatchLogsSubscriptionTransformer.class);

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<HashMap<String, String>> MAP_TYPE = new TypeReference<HashMap<String, String>>() {};

    @Override
    public Collection<CloudWatchLogsEvent> toClass(Record record) throws IOException {
        List<CloudWatchLogsEvent> result = new ArrayList<>();

        // uncompress the payload
        byte[] uncompressedPayload;
        try {
            uncompressedPayload = uncompress(record.getData().array());
        } catch (IOException e) {
            LOG.error("Unable to uncompress the record. Skipping it.", e);
            return result;
        }

        // get the JSON root node
        String jsonPayload = new String(uncompressedPayload, Charset.forName("UTF-8"));
        JsonNode rootNode;
        try {
            rootNode = JSON_OBJECT_MAPPER.readTree(new StringReader(jsonPayload));
        } catch (JsonProcessingException e) {
            LOG.error("Unable to parse the record as JSON. Skipping it.", e);
            return result;
        }

        // only process records whose type is DATA_MESSAGE
        JsonNode messageTypeNode = rootNode.get("messageType");
        if (messageTypeNode == null || !messageTypeNode.asText().equals("DATA_MESSAGE")) {
            LOG.warn("This record is not a data message. Skipping it.");
            return result;
        }

        // extract the common attributes for all the log events in the batch
        String owner = rootNode.get("owner").asText();
        String logGroup = rootNode.get("logGroup").asText();
        String logStream = rootNode.get("logStream").asText();

        // construct the log events
        for (JsonNode logEventNode : rootNode.get("logEvents")) {
            String id = logEventNode.get("id").asText();
            long timestamp = logEventNode.get("timestamp").asLong();
            String message = logEventNode.get("message").asText();

            Map<String, String> extractedFields = null;

            if (logEventNode.get("extractedFields") != null) {
                extractedFields = JSON_OBJECT_MAPPER.readValue(logEventNode.get("extractedFields").toString(), MAP_TYPE);
            }

            result.add(new CloudWatchLogsEvent(id, timestamp, message, extractedFields, owner, logGroup, logStream));
        }

        return result;
    }

    private static byte[] uncompress(byte[] compressedData) throws IOException {
        byte[] buffer = new byte[1024];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(compressedData))) {
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
            out.flush();
            return out.toByteArray();
        }
    }
}
