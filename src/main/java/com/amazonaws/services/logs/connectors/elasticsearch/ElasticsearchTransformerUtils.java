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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticsearchTransformerUtils {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    private ElasticsearchTransformerUtils() {}

    /**
     * Checks if the substring from the first occurrence of { is valid json and
     * returns the substring. Otherwise returns null.
     */
    public static String extractJson(String message) {
        int jsonStart = message.indexOf("{");

        if (jsonStart < 0) {
            return null;
        }

        String jsonSubString = message.substring(jsonStart);

        try {
            JsonNode rootNode = JSON_OBJECT_MAPPER.readTree(jsonSubString);
            if (rootNode.isValueNode()) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return jsonSubString;
    }
}
