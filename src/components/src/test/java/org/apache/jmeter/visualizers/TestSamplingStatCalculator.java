/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSamplingStatCalculator {

    private SamplingStatCalculator ssc;
    @BeforeEach
    public void setUp(){
        ssc = new SamplingStatCalculator("JUnit");
    }

    @Test
    public void testGetCurrentSample() {
        assertNotNull(ssc.getCurrentSample()); // probably needed to avoid NPEs with GUIs
    }

    @Test
    public void testGetAvgPageBytes() {
        SampleResult res = new SampleResult();
        assertEquals(0, ssc.getAvgPageBytes(), 0);
        res.setResponseData("abcdef", "UTF-8");
        ssc.addSample(res);
        res.setResponseData("abcde", "UTF-8");
        ssc.addSample(res);
        res.setResponseData("abcd", "UTF-8");
        ssc.addSample(res);
        assertEquals(5, ssc.getAvgPageBytes(), 0);
    }

    @Test
    public void testLazyMedian() {
        SampleResult res1 = new SampleResult(1000L, 10L);
        Sample s1 = ssc.addSample(res1);
        assertEquals(10L, s1.getMedian());
        assertEquals(10L, s1.getDistributionLine());

        SampleResult res2 = new SampleResult(1020L, 30L);
        Sample s2 = ssc.addSample(res2);

        SampleResult res3 = new SampleResult(1060L, 20L);
        Sample s3 = ssc.addSample(res3);

        assertEquals(20L, s3.getMedian());
        assertEquals(20L, s3.getDistributionLine());
        assertEquals(20L, ssc.getCurrentSample().getMedian());
        assertEquals(20L, ssc.getMedian().longValue());
    }

    @Test
    public void testSampleSerialization() throws Exception {
        SampleResult res = new SampleResult(1000L, 42L);
        Sample original = ssc.addSample(res);
        // Serialize without calling getMedian() first
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }
        Sample deserialized;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (Sample) ois.readObject();
        }
        assertEquals(42L, deserialized.getMedian());
        assertEquals(42L, deserialized.getDistributionLine());
    }
}
