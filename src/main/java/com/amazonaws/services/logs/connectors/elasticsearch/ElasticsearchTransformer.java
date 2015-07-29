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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.services.kinesis.connectors.elasticsearch.ElasticsearchObject;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsSubscriptionTransformer;
import com.amazonaws.util.json.JSONException;

/**
 * Transforms CloudWatchLogsEvent records to ElasticsearchObject records.
 */
public class ElasticsearchTransformer extends CloudWatchLogsSubscriptionTransformer<ElasticsearchObject> {

    private static final Log LOG = LogFactory.getLog(ElasticsearchTransformer.class);

    private static final DateTimeFormatter DAY_SUFFIX_FORMATTER = DateTimeFormat.forPattern("yyyy.MM.dd").withZone(
            DateTimeZone.UTC);

    private static final String INDEX_NAME_PREFIX = "cwl-";

    @Override
    public ElasticsearchObject fromClass(CloudWatchLogsEvent record) throws IOException {

        try {
            // convert the log event to an Elasticsearch document
            CloudWatchLogsElasticsearchDocument document = new CloudWatchLogsElasticsearchDocument(record);

            // daily indexes are used for bulk expiry
            String index = INDEX_NAME_PREFIX + DAY_SUFFIX_FORMATTER.print(document.getTimestamp());

            String type = document.getLogGroup();
            String id = document.getId();

            // this is the structured log event in JSON format
            String source = document.getSource();

            ElasticsearchObject elasticsearchObject = new ElasticsearchObject(index, type, id, source);
            elasticsearchObject.setCreate(true); // creates the index if it doesn't exist

            return elasticsearchObject;
        } catch (JSONException e) {
            String message = "Error serializing the Elasticsearch document to JSON: " + e.getMessage();
            LOG.error(message);
            throw new IOException(message, e);
        }
    }
}
