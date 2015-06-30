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

import java.util.Map;

/**
 * Defines the record that CloudWatch Logs uses for subscriptions.
 */
public class CloudWatchLogsEvent {

    private final String id;
    private final long timestamp;
    private final String message;
    private final Map<String, String> extractedFields;

    private final String owner;
    private final String logGroup;
    private final String logStream;

    public CloudWatchLogsEvent(String id, long timestamp, String message, Map<String, String> extractedFields,
            String owner, String logGroup, String logStream) {
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.extractedFields = extractedFields;
        this.owner = owner;
        this.logGroup = logGroup;
        this.logStream = logStream;
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

    public Map<String, String> getExtractedFields() {
        return extractedFields;
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
}
