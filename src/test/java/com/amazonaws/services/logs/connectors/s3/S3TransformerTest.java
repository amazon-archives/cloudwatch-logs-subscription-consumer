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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.logs.subscriptions.CloudWatchLogsEvent;
import com.amazonaws.services.logs.subscriptions.util.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class S3TransformerTest {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void transformAccessLog() throws IOException {
        S3Transformer classUnderTest = new S3Transformer();

        // load the example events
        byte[] data = TestUtils.getCompressedTestFile("/access-log-example.json");

        // execute
        List<CloudWatchLogsEvent> logEvents = new ArrayList<>(
                classUnderTest.toClass(new Record().withData(ByteBuffer.wrap(data))));

        List<byte[]> s3Entries = new ArrayList<>();

        for (CloudWatchLogsEvent logEvent : logEvents) {
            s3Entries.add(classUnderTest.fromClass(logEvent));
        }

        // verify
        JsonNode sourceNode;

        // event 1
        sourceNode = JSON_OBJECT_MAPPER.readTree(s3Entries.get(0));

        assertEquals("49545295115971876468408574808414755329919666212443258898", sourceNode.get("id").asText());
        assertEquals(1421116133213L, sourceNode.get("timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("owner").asText());
        assertEquals("Apache/access.log", sourceNode.get("logGroup").asText());
        assertEquals("i-c3f9bec9", sourceNode.get("logStream").asText());
        assertEquals("127.0.0.1 frank GET 200 4535", sourceNode.get("message").asText());

        assertEquals(200, sourceNode.get("extractedFields").get("status_code").asLong());
        assertEquals("GET", sourceNode.get("extractedFields").get("verb").asText());
        assertEquals(4535, sourceNode.get("extractedFields").get("response_size").asLong());
        assertEquals("frank", sourceNode.get("extractedFields").get("user").asText());
        assertEquals("127.0.0.1", sourceNode.get("extractedFields").get("ip").asText());

        // event 2
        sourceNode = JSON_OBJECT_MAPPER.readTree(s3Entries.get(1));

        assertEquals("49545295115971876468408574808465530214343480843939348498", sourceNode.get("id").asText());
        assertEquals(1421116143214L, sourceNode.get("timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("owner").asText());
        assertEquals("Apache/access.log", sourceNode.get("logGroup").asText());
        assertEquals("i-c3f9bec9", sourceNode.get("logStream").asText());
        assertEquals("127.0.0.1 alice POST 404 34", sourceNode.get("message").asText());

        assertEquals(404, sourceNode.get("extractedFields").get("status_code").asLong());
        assertEquals("POST", sourceNode.get("extractedFields").get("verb").asText());
        assertEquals(34, sourceNode.get("extractedFields").get("response_size").asLong());
        assertEquals("alice", sourceNode.get("extractedFields").get("user").asText());
        assertEquals("127.0.0.1", sourceNode.get("extractedFields").get("ip").asText());
    }

    @Test
    public void transformCloudTrailLog() throws IOException {
        S3Transformer classUnderTest = new S3Transformer();

        // load the example events
        byte[] data = TestUtils.getCompressedTestFile("/aws-cloudtrail-log-example.json");

        // execute
        List<CloudWatchLogsEvent> logEvents = new ArrayList<>(
                classUnderTest.toClass(new Record().withData(ByteBuffer.wrap(data))));

        List<byte[]> s3Entries = new ArrayList<>();

        for (CloudWatchLogsEvent logEvent : logEvents) {
            s3Entries.add(classUnderTest.fromClass(logEvent));
        }

        // verify
        JsonNode sourceNode;

        // event 1
        sourceNode = JSON_OBJECT_MAPPER.readTree(s3Entries.get(0));

        assertEquals("49545295115971876468408574808465530214343480843939348498", sourceNode.get("id").asText());
        assertEquals(1421116143214L, sourceNode.get("timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("owner").asText());
        assertEquals("CloudTrail", sourceNode.get("logGroup").asText());
        assertEquals("123456789012_CloudTrail_us-east-1", sourceNode.get("logStream").asText());

        assertTrue(sourceNode.get("message").asText().startsWith("{"));
        assertTrue(sourceNode.get("message").asText().endsWith("}"));

        assertNull(sourceNode.get("extractedFields"));

        // event 2
        sourceNode = JSON_OBJECT_MAPPER.readTree(s3Entries.get(1));

        assertEquals("49545295115971876468408574808465530214343150403450640305", sourceNode.get("id").asText());
        assertEquals(1421116143456L, sourceNode.get("timestamp").asLong());
        assertEquals("123456789012", sourceNode.get("owner").asText());
        assertEquals("CloudTrail", sourceNode.get("logGroup").asText());
        assertEquals("123456789012_CloudTrail_us-east-1", sourceNode.get("logStream").asText());

        assertTrue(sourceNode.get("message").asText().startsWith("{"));
        assertTrue(sourceNode.get("message").asText().endsWith("}"));

        assertNull(sourceNode.get("extractedFields"));
    }
}
