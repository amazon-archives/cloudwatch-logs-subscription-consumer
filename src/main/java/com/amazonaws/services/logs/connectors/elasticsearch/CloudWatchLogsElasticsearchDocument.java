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

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.lang3.StringUtils;

import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Defines the log event structure that would be sent to Elasticsearch.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudWatchLogsElasticsearchDocument implements Serializable {

    private static final long serialVersionUID = -8898041979675208782L;

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    private static final Log LOG = LogFactory.getLog(CloudWatchLogsElasticsearchDocument.class);

    private String id;
    private long timestamp;
    private String message;
    private String fields;

    private String owner;
    private String logGroup;
    private String logStream;

    public CloudWatchLogsElasticsearchDocument() {
        // required by the Jackson JSON mapper
    }

    public CloudWatchLogsElasticsearchDocument(CloudWatchLogsEvent event) {
        this.id = event.getId();
        this.timestamp = event.getTimestamp();
        this.message = event.getMessage();
        this.fields = getFields(event.getMessage(), event.getExtractedFields());
        this.owner = event.getOwner();
        this.logGroup = event.getLogGroup();
        this.logStream = event.getLogStream();
    }

    /**
     *
     * Determines which additional fields get put into the Elasticsearch document.
     */
    private String getFields(String message, Map<String, String> extractedFields) {
        // if extractedFields are available from CloudWatch Logs, use them as Elasticsearch fields
        if (extractedFields != null && extractedFields.size() > 0) {
            JSONObject extractedFieldsInJson = new JSONObject();

            try {
                for (Map.Entry<String, String> entry : extractedFields.entrySet()) {
                    String fieldName = entry.getKey();
                    String value = entry.getValue();

                    if (StringUtils.isNumeric(value)) {
                        extractedFieldsInJson.put(fieldName, Double.parseDouble(value));
                    } else {
                        extractedFieldsInJson.put(fieldName, value);
                    }
                }

                return extractedFieldsInJson.toString();
            } catch (JSONException e) {
                LOG.error("Unable to convert extractedFields to JSON: " + e.getMessage());
                return null;
            }
        }

        // if the message is valid JSON, use the message as is for Elasticsearch fields
        if (isMessageValidJson(message)) {
            return message;
        }

        // if there are no extractedFields and the message is not valid JSON, don't emit any Elasticsearch fields
        return null;
    }

    public static boolean isMessageValidJson(String message) {

        try {
            JSON_OBJECT_MAPPER.readTree(message);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @JsonProperty("@id")
    public String getId() {
        return id;
    }

    @JsonProperty("@timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("@message")
    public String getMessage() {
        return message;
    }

    @JsonRawValue
    @JsonProperty("$")
    public String getFields() {
        return fields;
    }

    @JsonProperty("@owner")
    public String getOwner() {
        return owner;
    }

    @JsonProperty("@log_group")
    public String getLogGroup() {
        return logGroup;
    }

    @JsonProperty("@log_stream")
    public String getLogStream() {
        return logStream;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setLogGroup(String logGroup) {
        this.logGroup = logGroup;
    }

    public void setLogStream(String logStream) {
        this.logStream = logStream;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }
}
