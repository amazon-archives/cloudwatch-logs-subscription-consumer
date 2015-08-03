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
package com.amazonaws.services.logs.connectors.elasticsearch;

import java.util.Map;

import org.elasticsearch.common.lang3.StringUtils;

import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

/**
 * Defines the log event structure that would be sent to Elasticsearch.
 */
public class CloudWatchLogsElasticsearchDocument {

    private static final String JSON_FIELD_NAME_PREFIX = "$";

    private final String id;
    private final long timestamp;
    private final String message;

    private final String owner;
    private final String logGroup;
    private final String logStream;

    private final String source;

    public CloudWatchLogsElasticsearchDocument(CloudWatchLogsEvent event) throws JSONException {
        JSONObject json = getFields(event.getMessage(), event.getExtractedFields());
        json.put("@id", event.getId());
        json.put("@timestamp", event.getTimestamp());
        json.put("@message", event.getMessage());
        json.put("@owner", event.getOwner());
        json.put("@log_group", event.getLogGroup());
        json.put("@log_stream", event.getLogStream());

        this.source = json.toString();

        this.id = event.getId();
        this.timestamp = event.getTimestamp();
        this.message = event.getMessage();
        this.owner = event.getOwner();
        this.logGroup = event.getLogGroup();
        this.logStream = event.getLogStream();
    }

    /**
     * Determines which additional fields get put into the Elasticsearch document.
     */
    private JSONObject getFields(String message, Map<String, String> extractedFields) throws JSONException {

        // if extractedFields are available from CloudWatch Logs, use them as Elasticsearch fields
        if (extractedFields != null && extractedFields.size() > 0) {
            JSONObject extractedFieldsInJson = new JSONObject();

            for (Map.Entry<String, String> entry : extractedFields.entrySet()) {
                String fieldName = entry.getKey();
                String value = entry.getValue();

                if (value == null) {
                    // nothing to add
                    continue;
                }

                if (StringUtils.isNumeric(value)) {
                    // the field value is a number - put it as a double
                    extractedFieldsInJson.put(fieldName, Double.parseDouble(value));
                    continue;
                }

                String jsonSubString = ElasticsearchTransformerUtils.extractJson(value);

                if (jsonSubString != null) {
                    // the field value contains valid json - copy the json to a new Elasticsearch object field
                    extractedFieldsInJson.put(JSON_FIELD_NAME_PREFIX + fieldName, new JSONObject(jsonSubString));
                }

                // put the raw extracted field as a string
                extractedFieldsInJson.put(fieldName, value);
            }

            return extractedFieldsInJson;
        }

        // if the message is valid JSON, use the message as is for Elasticsearch fields
        if (ElasticsearchTransformerUtils.extractJson(message) != null) {
            return new JSONObject(message);
        }

        // if there are no extractedFields and the message is not valid JSON, don't emit any Elasticsearch fields
        return new JSONObject();
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getOwner() {
        return owner;
    }

    public String getLogGroup() {
        return logGroup;
    }

    public String getLogStream() {
        return logStream;
    }

    public String getSource() {
        return source;
    }
}
