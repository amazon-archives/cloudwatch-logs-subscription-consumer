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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ElasticsearchTransformerUtilsTest {

    @Test
    public void extractJson() {
        assertNull(ElasticsearchTransformerUtils.extractJson("2"));
        assertNull(ElasticsearchTransformerUtils.extractJson("\"value\""));
        assertNull(ElasticsearchTransformerUtils.extractJson("100 \"value\""));
        assertNull(ElasticsearchTransformerUtils.extractJson("{ \"key\": 100"));
        assertNull(ElasticsearchTransformerUtils.extractJson("{ \"key\": \"100\""));
        assertNull(ElasticsearchTransformerUtils.extractJson("{ \"tree\": { \"key1\": \"100\" \"key2\": \"200\" } }"));
        assertNull(ElasticsearchTransformerUtils.extractJson("outside { \"tree\": { \"key1\": \"100\" \"key2\": \"200\" } }"));
        assertNull(ElasticsearchTransformerUtils.extractJson("{ \"tree\": { \"key1\": \"100\" \"key2\": \"200\" } } outside"));
        assertNull(ElasticsearchTransformerUtils.extractJson("{ \"array\": [ }"));

        assertNotNull(ElasticsearchTransformerUtils.extractJson("{ \"key\": 100 }"));
        assertNotNull(ElasticsearchTransformerUtils.extractJson("{ \"key\": \"100\" }"));
        assertNotNull(ElasticsearchTransformerUtils.extractJson("{ \"key1\": \"100\", \"key2\": \"200\" }"));
        assertNotNull(ElasticsearchTransformerUtils.extractJson("{ \"tree\": { \"key1\": \"100\", \"key2\": \"200\" } }"));
        assertNotNull(ElasticsearchTransformerUtils.extractJson("{ \"tree\": \n{ \"key1\": \"100\", \"key2\": \"200\" } }"));
        assertNotNull(ElasticsearchTransformerUtils.extractJson("   { \"tree\":\n \n{ \"key1\": \"100\",\n \"key2\": \"200\" }\n }\n   "));
        assertNotNull(ElasticsearchTransformerUtils.extractJson("{ \"array\": [ { \"key1\": \"100\", \"key2\": \"200\" } ] }"));
        assertNotNull(ElasticsearchTransformerUtils.extractJson("{ \"array\": [ { \"key1\": \"100\" }, { \"key2\": \"200\" } ] }"));
        assertNotNull(ElasticsearchTransformerUtils.extractJson("{ \"array\": [] }"));

        assertEquals("{ \"key\": \"100\" }", ElasticsearchTransformerUtils.extractJson("{ \"key\": \"100\" }"));
        assertEquals("{ \"key\": \"100\" }",
                ElasticsearchTransformerUtils.extractJson("Received: { \"key\": \"100\" }"));
        assertEquals("{ \"key\": \"100\" }",
                ElasticsearchTransformerUtils.extractJson("Received event:     { \"key\": \"100\" }"));
        assertEquals(
                "{ \"array\": [ { \"key1\": \"100\" }, { \"key2\": \"200\" } ] }",
                ElasticsearchTransformerUtils.extractJson("Received event: { \"array\": [ { \"key1\": \"100\" }, { \"key2\": \"200\" } ] }"));
    }
}
