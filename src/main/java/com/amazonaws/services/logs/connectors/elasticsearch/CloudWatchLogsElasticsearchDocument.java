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

/**
 * Defines the log event structure that would be sent to Elasticsearch.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudWatchLogsElasticsearchDocument implements Serializable {

    private static final long serialVersionUID = -8898041979675208782L;

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

                    if (value == null) {
                        // nothing to add
                        continue;
                    } else if (ElasticsearchTransformerUtils.isMessageValidJson(value)) {
                        // the field value is valid json - put it as a nested object
                        extractedFieldsInJson.put(fieldName, new JSONObject(value));
                    } else if (StringUtils.isNumeric(value)) {
                        // the field value is a number - put it as a double
                        extractedFieldsInJson.put(fieldName, Double.parseDouble(value));
                    } else {
                        // else put the field as a string
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
        if (ElasticsearchTransformerUtils.isMessageValidJson(message)) {
            return message;
        }

        // if there are no extractedFields and the message is not valid JSON, don't emit any Elasticsearch fields
        return null;
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
}
