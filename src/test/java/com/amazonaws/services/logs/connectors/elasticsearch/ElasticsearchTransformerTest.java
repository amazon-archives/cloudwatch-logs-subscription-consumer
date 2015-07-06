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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.kinesis.connectors.elasticsearch.ElasticsearchObject;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;
import com.amazonaws.services.logs.subscriptions.util.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticsearchTransformerTest {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void transformAccessLog() throws IOException {
        ElasticsearchTransformer classUnderTest = new ElasticsearchTransformer();

        // load the example events
        byte[] data = TestUtils.getCompressedTestFile("/access-log-example.json");

        // execute
        List<CloudWatchLogsEvent> logEvents = new ArrayList<>(
                classUnderTest.toClass(new Record().withData(ByteBuffer.wrap(data))));

        List<ElasticsearchObject> elasticsearchDocuments = new ArrayList<>();

        for (CloudWatchLogsEvent logEvent : logEvents) {
            elasticsearchDocuments.add(classUnderTest.fromClass(logEvent));
        }

        // verify
        assertEquals("49545295115971876468408574808414755329919666212443258898", elasticsearchDocuments.get(0).getId());
        assertEquals("49545295115971876468408574808465530214343480843939348498", elasticsearchDocuments.get(1).getId());

        assertEquals("cwl-2015.01.13", elasticsearchDocuments.get(0).getIndex());
        assertEquals("cwl-2015.01.13", elasticsearchDocuments.get(1).getIndex());

        assertEquals("Apache/access.log", elasticsearchDocuments.get(0).getType());
        assertEquals("Apache/access.log", elasticsearchDocuments.get(1).getType());

        JsonNode sourceNode;

        // event 1
        sourceNode = JSON_OBJECT_MAPPER.readTree(new StringReader(elasticsearchDocuments.get(0).getSource()));

        assertEquals("49545295115971876468408574808414755329919666212443258898", sourceNode.get("@id").asText());
        assertEquals(1421116133213L, sourceNode.get("@timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("@owner").asText());
        assertEquals("Apache/access.log", sourceNode.get("@log_group").asText());
        assertEquals("i-c3f9bec9", sourceNode.get("@log_stream").asText());
        assertEquals("127.0.0.1 frank GET 200 4535", sourceNode.get("@message").asText());

        assertEquals(200, sourceNode.get("$").get("status_code").asLong());
        assertEquals("GET", sourceNode.get("$").get("verb").asText());
        assertEquals(4535, sourceNode.get("$").get("response_size").asLong());
        assertEquals("frank", sourceNode.get("$").get("user").asText());
        assertEquals("127.0.0.1", sourceNode.get("$").get("ip").asText());

        // event 2
        sourceNode = JSON_OBJECT_MAPPER.readTree(new StringReader(elasticsearchDocuments.get(1).getSource()));

        assertEquals("49545295115971876468408574808465530214343480843939348498", sourceNode.get("@id").asText());
        assertEquals(1421116143214L, sourceNode.get("@timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("@owner").asText());
        assertEquals("Apache/access.log", sourceNode.get("@log_group").asText());
        assertEquals("i-c3f9bec9", sourceNode.get("@log_stream").asText());
        assertEquals("127.0.0.1 alice POST 404 34", sourceNode.get("@message").asText());

        assertEquals(404, sourceNode.get("$").get("status_code").asLong());
        assertEquals("POST", sourceNode.get("$").get("verb").asText());
        assertEquals(34, sourceNode.get("$").get("response_size").asLong());
        assertEquals("alice", sourceNode.get("$").get("user").asText());
        assertEquals("127.0.0.1", sourceNode.get("$").get("ip").asText());
    }

    @Test
    public void transformCloudTrailLog() throws IOException {
        ElasticsearchTransformer classUnderTest = new ElasticsearchTransformer();

        // load the example events
        byte[] data = TestUtils.getCompressedTestFile("/aws-cloudtrail-log-example.json");

        // execute
        List<CloudWatchLogsEvent> logEvents = new ArrayList<>(
                classUnderTest.toClass(new Record().withData(ByteBuffer.wrap(data))));

        List<ElasticsearchObject> elasticsearchDocuments = new ArrayList<>();

        for (CloudWatchLogsEvent logEvent : logEvents) {
            elasticsearchDocuments.add(classUnderTest.fromClass(logEvent));
        }

        // verify
        assertEquals("49545295115971876468408574808465530214343480843939348498", elasticsearchDocuments.get(0).getId());
        assertEquals("49545295115971876468408574808465530214343150403450640305", elasticsearchDocuments.get(1).getId());

        assertEquals("cwl-2015.01.13", elasticsearchDocuments.get(0).getIndex());
        assertEquals("cwl-2015.01.13", elasticsearchDocuments.get(1).getIndex());

        assertEquals("CloudTrail", elasticsearchDocuments.get(0).getType());
        assertEquals("CloudTrail", elasticsearchDocuments.get(1).getType());

        JsonNode sourceNode;

        // event 1
        sourceNode = JSON_OBJECT_MAPPER.readTree(new StringReader(elasticsearchDocuments.get(0).getSource()));

        assertEquals("49545295115971876468408574808465530214343480843939348498", sourceNode.get("@id").asText());
        assertEquals(1421116143214L, sourceNode.get("@timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("@owner").asText());
        assertEquals("CloudTrail", sourceNode.get("@log_group").asText());
        assertEquals("123456789012_CloudTrail_us-east-1", sourceNode.get("@log_stream").asText());
        assertTrue(sourceNode.get("@message").asText().startsWith("{"));
        assertTrue(sourceNode.get("@message").asText().endsWith("}"));

        assertEquals("1.02", sourceNode.get("$").get("eventVersion").asText());
        assertEquals("Root", sourceNode.get("$").get("userIdentity").get("type").asText());
        assertEquals("signin.amazonaws.com", sourceNode.get("$").get("eventSource").asText());

        // event 2
        sourceNode = JSON_OBJECT_MAPPER.readTree(new StringReader(elasticsearchDocuments.get(1).getSource()));

        assertEquals("49545295115971876468408574808465530214343150403450640305", sourceNode.get("@id").asText());
        assertEquals(1421116143456L, sourceNode.get("@timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("@owner").asText());
        assertEquals("CloudTrail", sourceNode.get("@log_group").asText());
        assertEquals("123456789012_CloudTrail_us-east-1", sourceNode.get("@log_stream").asText());
        assertTrue(sourceNode.get("@message").asText().startsWith("{"));
        assertTrue(sourceNode.get("@message").asText().endsWith("}"));

        assertEquals("1.02", sourceNode.get("$").get("eventVersion").asText());
        assertEquals("Root", sourceNode.get("$").get("userIdentity").get("type").asText());
        assertEquals("cloudtrail.amazonaws.com", sourceNode.get("$").get("eventSource").asText());
    }

    @Test
    public void transformLambdaLog() throws IOException {
        ElasticsearchTransformer classUnderTest = new ElasticsearchTransformer();

        // load the example events
        byte[] data = TestUtils.getCompressedTestFile("/aws-lambda-log-example.json");

        // execute
        List<CloudWatchLogsEvent> logEvents = new ArrayList<>(
                classUnderTest.toClass(new Record().withData(ByteBuffer.wrap(data))));

        List<ElasticsearchObject> elasticsearchDocuments = new ArrayList<>();

        for (CloudWatchLogsEvent logEvent : logEvents) {
            elasticsearchDocuments.add(classUnderTest.fromClass(logEvent));
        }

        // verify
        assertEquals("49545295115971876468408574808414755329919666212443258898", elasticsearchDocuments.get(0).getId());
        assertEquals("49545295115971876468408574808465530214343150403450640305", elasticsearchDocuments.get(1).getId());

        assertEquals("cwl-2015.01.13", elasticsearchDocuments.get(0).getIndex());
        assertEquals("cwl-2015.01.13", elasticsearchDocuments.get(1).getIndex());

        assertEquals("/aws/lambda/HelloWorld", elasticsearchDocuments.get(0).getType());
        assertEquals("/aws/lambda/HelloWorld", elasticsearchDocuments.get(1).getType());

        JsonNode sourceNode;

        // event 1
        sourceNode = JSON_OBJECT_MAPPER.readTree(new StringReader(elasticsearchDocuments.get(0).getSource()));

        assertEquals("49545295115971876468408574808414755329919666212443258898", sourceNode.get("@id").asText());
        assertEquals(1421116133213L, sourceNode.get("@timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("@owner").asText());
        assertEquals("/aws/lambda/HelloWorld", sourceNode.get("@log_group").asText());
        assertEquals("2015/06/30/1f77bc4743204b22b0d42cf3b85f40c7", sourceNode.get("@log_stream").asText());
        assertEquals(
                "2015-01-13T02:28:53.213Z c342155b-1ec0-11e5-b0e2-f317438eb2f6 { \"key1\": 100, \"key2\": \"value\", \"key3\": { \"key4\": \"level2\" } }",
                sourceNode.get("@message").asText());

        assertEquals("2015-01-13T02:28:53.213Z", sourceNode.get("$").get("timestamp").asText());
        assertEquals("c342155b-1ec0-11e5-b0e2-f317438eb2f6", sourceNode.get("$").get("request_id").asText());
        assertEquals(100, sourceNode.get("$").get("event").get("key1").asLong());
        assertEquals("value", sourceNode.get("$").get("event").get("key2").asText());
        assertEquals("level2", sourceNode.get("$").get("event").get("key3").get("key4").asText());

        // event 2
        sourceNode = JSON_OBJECT_MAPPER.readTree(new StringReader(elasticsearchDocuments.get(1).getSource()));

        assertEquals("49545295115971876468408574808465530214343150403450640305", sourceNode.get("@id").asText());
        assertEquals(1421116143456L, sourceNode.get("@timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("@owner").asText());
        assertEquals("/aws/lambda/HelloWorld", sourceNode.get("@log_group").asText());
        assertEquals("2015/06/30/1f77bc4743204b22b0d42cf3b85f40c7", sourceNode.get("@log_stream").asText());
        assertEquals("2015-01-13T02:29:03.456Z c342155b-1ec0-11e5-b0e2-f317438eb2f6 Hello World",
                sourceNode.get("@message").asText());

        assertEquals("2015-01-13T02:29:03.456Z", sourceNode.get("$").get("timestamp").asText());
        assertEquals("c342155b-1ec0-11e5-b0e2-f317438eb2f6", sourceNode.get("$").get("request_id").asText());
        assertEquals("Hello World", sourceNode.get("$").get("event").asText());
    }
}
