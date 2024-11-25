/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.testing;

import org.gradle.api.NonNullApi;
import org.gradle.api.internal.tasks.testing.results.DefaultTestResult;
import org.gradle.api.internal.tasks.testing.results.TestListenerInternal;
import org.gradle.api.tasks.testing.TestEventReporter;
import org.gradle.api.tasks.testing.TestFailure;
import org.gradle.api.tasks.testing.TestFailureDetails;
import org.gradle.api.tasks.testing.TestOutputEvent;
import org.gradle.api.tasks.testing.TestResult;

import java.time.Instant;
import java.util.Collections;

@NonNullApi
public class DefaultTestEventReporter implements TestEventReporter {

    protected final TestListenerInternal listener;
    protected final TestDescriptorInternal testDescriptor;

    private long startTime;

    public DefaultTestEventReporter(TestListenerInternal listener, TestDescriptorInternal testDescriptor) {
        this.listener = listener;
        this.testDescriptor = testDescriptor;
    }

    @Override
    public void started(Instant startTime) {
        this.startTime = startTime.toEpochMilli();
        listener.started(testDescriptor, new TestStartEvent(startTime.toEpochMilli(), testDescriptor.getParent() == null ? null : testDescriptor.getParent().getId()));
    }

    @Override
    public void output(Instant logTime, TestOutputEvent.Destination destination, String output) {
        listener.output(testDescriptor, new DefaultTestOutputEvent(logTime.toEpochMilli(), destination, output));
    }

    @Override
    public void succeeded(Instant endTime) {
        listener.completed(testDescriptor, new DefaultTestResult(TestResult.ResultType.SUCCESS, startTime, endTime.toEpochMilli(), 1, 1, 0, Collections.emptyList()), new TestCompleteEvent(endTime.toEpochMilli(), TestResult.ResultType.SUCCESS));
    }

    @Override
    public void skipped(Instant endTime) {
        listener.completed(testDescriptor, new DefaultTestResult(TestResult.ResultType.SKIPPED, startTime, endTime.toEpochMilli(), 1, 0, 0, Collections.emptyList()), new TestCompleteEvent(endTime.toEpochMilli(), TestResult.ResultType.SKIPPED));
    }

    @Override
    public void failed(Instant endTime) {
        listener.completed(testDescriptor, new DefaultTestResult(TestResult.ResultType.FAILURE, startTime, endTime.toEpochMilli(), 1, 0, 1, Collections.emptyList()), new TestCompleteEvent(endTime.toEpochMilli(), TestResult.ResultType.FAILURE));
    }

    @Override
    public void failed(Instant endTime, String message) {
        TestFailureDetails failureDetails = new DefaultTestFailureDetails(message, Throwable.class.getName(), "", false, false, null, null, null, null);
        TestFailure testFailure = new DefaultTestFailure(new Throwable(message), failureDetails, Collections.emptyList());
        listener.completed(testDescriptor, new DefaultTestResult(TestResult.ResultType.FAILURE, startTime, endTime.toEpochMilli(), 1, 0, 1, Collections.singletonList(testFailure)), new TestCompleteEvent(endTime.toEpochMilli(), TestResult.ResultType.FAILURE));
    }

    @Override
    public void close() {
        // do nothing
    }
}
