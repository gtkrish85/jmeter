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

import java.util.concurrent.TimeUnit;

import org.apache.jmeter.samplers.SampleResult;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(value = 1, jvmArgsPrepend = {"-Xmx128m"})
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SamplingStatCalculatorBenchmark {
    SamplingStatCalculator calculator;
    SampleResult[] sampleResults;
    int index;

    @Param({"1000"})
    int distinctValues = 1000;

    @Setup
    public void setup() {
        calculator = new SamplingStatCalculator("benchmark");
        sampleResults = new SampleResult[distinctValues];
        long baseTime = 1_000_000_000L;
        for (int i = 0; i < distinctValues; i++) {
            long elapsed = (i + 1) * 2L;
            long start = baseTime + i * 10L;
            long end = start + elapsed;
            SampleResult res = SampleResult.createTestSample(start, end);
            res.setBytes(1024L);
            res.setSentBytes(256L);
            res.setSuccessful(true);
            res.setSampleCount(1);
            sampleResults[i] = res;
            calculator.addSample(res);
        }
    }

    @Benchmark
    public Sample addSample() {
        int idx = index;
        if (++index >= distinctValues) {
            index = 0;
        }
        return calculator.addSample(sampleResults[idx]);
    }

    @Benchmark
    public Number getMedian() {
        return calculator.getMedian();
    }

    @Benchmark
    public Number getPercentPoint50() {
        return calculator.getPercentPoint(0.50);
    }

    @Benchmark
    public Number getPercentPoint90() {
        return calculator.getPercentPoint(0.90);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SamplingStatCalculatorBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .detectJvmArgs()
                .build();
        new Runner(opt).run();
    }
}
