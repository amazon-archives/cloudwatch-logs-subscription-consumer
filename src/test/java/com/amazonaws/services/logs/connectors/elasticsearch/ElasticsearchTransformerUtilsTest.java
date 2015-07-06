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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ElasticsearchTransformerUtilsTest {

    @Test
    public void isMessageValidJson() {
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("2"));
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("\"value\""));
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("100 \"value\""));
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("{ \"key\": 100"));
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("{ \"key\": \"100\""));
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("{ \"tree\": { \"key1\": \"100\" \"key2\": \"200\" } }"));
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("outside { \"tree\": { \"key1\": \"100\" \"key2\": \"200\" } }"));
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("{ \"tree\": { \"key1\": \"100\" \"key2\": \"200\" } } outside"));
        assertFalse(ElasticsearchTransformerUtils.isMessageValidJson("{ \"array\": [ }"));

        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("{ \"key\": 100 }"));
        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("{ \"key\": \"100\" }"));
        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("{ \"key1\": \"100\", \"key2\": \"200\" }"));
        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("{ \"tree\": { \"key1\": \"100\", \"key2\": \"200\" } }"));
        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("{ \"tree\": \n{ \"key1\": \"100\", \"key2\": \"200\" } }"));
        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("   { \"tree\":\n \n{ \"key1\": \"100\",\n \"key2\": \"200\" }\n }\n   "));
        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("{ \"array\": [ { \"key1\": \"100\", \"key2\": \"200\" } ] }"));
        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("{ \"array\": [ { \"key1\": \"100\" }, { \"key2\": \"200\" } ] }"));
        assertTrue(ElasticsearchTransformerUtils.isMessageValidJson("{ \"array\": [] }"));
    }
}
