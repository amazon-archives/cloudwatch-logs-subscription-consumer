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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.logs.subscriptions.util.TestUtils;

public class SubscriptionsParsingTest {

    @Test
    public void pareseAccessLog() throws IOException {
        CloudWatchLogsSubscriptionTransformer<String> classUnderTest = new CloudWatchLogsSubscriptionToStringTransformer();

        // load the example events
        byte[] data = TestUtils.getCompressedTestFile("/access-log-example.json");

        // execute
        List<CloudWatchLogsEvent> logEvents = new ArrayList<>(
                classUnderTest.toClass(new Record().withData(ByteBuffer.wrap(data))));

        // verify
        for (CloudWatchLogsEvent logEvent : logEvents) {
            assertEquals("123456789012", logEvent.getOwner());
            assertEquals("Apache/access.log", logEvent.getLogGroup());
            assertEquals("i-c3f9bec9", logEvent.getLogStream());
        }

        assertEquals("49545295115971876468408574808414755329919666212443258898", logEvents.get(0).getId());
        assertEquals(1421116133213L, logEvents.get(0).getTimestamp());
        assertEquals("127.0.0.1 frank GET 200 4535", logEvents.get(0).getMessage());
        assertEquals("127.0.0.1", logEvents.get(0).getExtractedFields().get("ip"));
        assertEquals("frank", logEvents.get(0).getExtractedFields().get("user"));
        assertEquals("GET", logEvents.get(0).getExtractedFields().get("verb"));
        assertEquals("200", logEvents.get(0).getExtractedFields().get("status_code"));
        assertEquals("4535", logEvents.get(0).getExtractedFields().get("response_size"));

        assertEquals("49545295115971876468408574808465530214343480843939348498", logEvents.get(1).getId());
        assertEquals(1421116143214L, logEvents.get(1).getTimestamp());
        assertEquals("127.0.0.1 alice POST 404 34", logEvents.get(1).getMessage());
        assertEquals("127.0.0.1", logEvents.get(1).getExtractedFields().get("ip"));
        assertEquals("alice", logEvents.get(1).getExtractedFields().get("user"));
        assertEquals("POST", logEvents.get(1).getExtractedFields().get("verb"));
        assertEquals("404", logEvents.get(1).getExtractedFields().get("status_code"));
        assertEquals("34", logEvents.get(1).getExtractedFields().get("response_size"));
    }

    @Test
    public void parseCloudTrailLog() throws IOException {
        CloudWatchLogsSubscriptionTransformer<String> classUnderTest = new CloudWatchLogsSubscriptionToStringTransformer();

        // load the example events
        byte[] data = TestUtils.getCompressedTestFile("/aws-cloudtrail-log-example.json");

        // execute
        List<CloudWatchLogsEvent> logEvents = new ArrayList<>(
                classUnderTest.toClass(new Record().withData(ByteBuffer.wrap(data))));

        // verify
        for (CloudWatchLogsEvent logEvent : logEvents) {
            assertEquals("123456789012", logEvent.getOwner());
            assertEquals("CloudTrail", logEvent.getLogGroup());
            assertEquals("123456789012_CloudTrail_us-east-1", logEvent.getLogStream());
        }

        assertEquals("49545295115971876468408574808465530214343480843939348498", logEvents.get(0).getId());
        assertEquals(1421116143214L, logEvents.get(0).getTimestamp());
        assertNull(logEvents.get(0).getExtractedFields());

        assertEquals("49545295115971876468408574808465530214343150403450640305", logEvents.get(1).getId());
        assertEquals(1421116143456L, logEvents.get(1).getTimestamp());
        assertNull(logEvents.get(0).getExtractedFields());
    }

    @Test
    public void parseControlMessage() throws IOException {
        verifyRecordIsSkipped(TestUtils.getCompressedTestFile("/control-message-example.json"));
    }

    @Test
    public void parseRecordWithNoMessageType() throws IOException {
        verifyRecordIsSkipped(TestUtils.getCompressedTestFile("/no-message-type-example.json"));
    }

    @Test
    public void parseRecordWithInvalidJson() throws IOException {
        verifyRecordIsSkipped(TestUtils.getCompressedTestFile("/invalid-json-example.json"));
    }

    private void verifyRecordIsSkipped(byte[] data) throws IOException {
        CloudWatchLogsSubscriptionTransformer<String> classUnderTest = new CloudWatchLogsSubscriptionToStringTransformer();

        // execute
        List<CloudWatchLogsEvent> logEvents = new ArrayList<>(
                classUnderTest.toClass(new Record().withData(ByteBuffer.wrap(data))));

        // verify that the control message gets ignored
        assertNotNull(logEvents);
        assertEquals(0, logEvents.size());
    }

    private class CloudWatchLogsSubscriptionToStringTransformer extends CloudWatchLogsSubscriptionTransformer<String> {

        @Override
        public String fromClass(CloudWatchLogsEvent logEvent) throws IOException {
            return logEvent.getTimestamp() + " " + logEvent.getMessage();
        }
    }
}
