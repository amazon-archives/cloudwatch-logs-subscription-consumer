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
package com.amazonaws.services.logs.subscriptions.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;

public class TestUtils {

    private TestUtils() {}

    public static byte[] getCompressedTestFile(String filename) throws IOException {
        byte[] uncompressedData = FileUtils.readFileToByteArray(new File(TestUtils.class.getResource(filename)
            .getFile()));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);

        try {
            gzipOutputStream.write(uncompressedData);
            gzipOutputStream.close();

            return byteArrayOutputStream.toByteArray();
        } finally {
            byteArrayOutputStream.close();
        }
    }
}
