package com.example;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.testing.GroupTestEventReporter;
import org.gradle.api.tasks.testing.TestEventReporter;
import org.gradle.api.tasks.testing.TestEventReporterFactory;
import org.gradle.api.tasks.testing.TestOutputEvent;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

abstract class CustomTest extends DefaultTask {
    @Inject
    public CustomTest() {
    }

    @Input
    @Option(option="fail", description = "Tells the task to demonstrate failures.")
    public abstract Property<Boolean> getFail();

    @Inject
    public abstract TestEventReporterFactory getTestEventReporterFactory();

    @TaskAction
    void runTests() {
        // This test is a demonstration of generating the proper test events.
        // Simulate a variety of conditions (may fail sometimes unless --fail is specified)
        try (GroupTestEventReporter root = getTestEventReporterFactory().createTestEventReporter(getName())) {
            root.started(Instant.now());
            boolean hasAnyFailures = false;
            Random random = new Random();

            // If requested, always demonstrate a failing test
            if (getFail().get()) {
                try (GroupTestEventReporter suite = root.reportTestGroup("FailingSuite")) {
                    suite.started(Instant.now());
                    try (TestEventReporter test = suite.reportTest("failingTest", "failingTest()")) {
                        test.started(Instant.now());
                        test.failed(Instant.now(), "This is a test failure");
                    }
                    suite.failed(Instant.now(), "This is additional message for the suite failure");
                }
                hasAnyFailures = true;
            }

            // Demonstrate multiple test suites with tests with different outcomes
            Collection<String> suiteNames = Arrays.asList("MyTestSuite", "MyOtherTestSuite", "AnotherTestSuite");
            for (String suiteName : suiteNames) {
                try (GroupTestEventReporter suite = root.reportTestGroup(suiteName)) {
                    suite.started(Instant.now());
                    int testCount = random.nextInt(9) + 1;

                    for (int i=0; i<testCount; i++) {
                        try (TestEventReporter test = suite.reportTest("test" + i, "test(" + i + ")")) {
                            test.started(Instant.now());

                            test.output(Instant.now(), TestOutputEvent.Destination.StdOut, "This is some standard output");
                            test.output(Instant.now(), TestOutputEvent.Destination.StdErr, "This is some standard error");

                            // randomly skip some tests
                            if (random.nextBoolean()) {
                                test.skipped(Instant.now());
                            } else {
                                test.succeeded(Instant.now());
                            }
                        }
                    }
                    suite.succeeded(Instant.now());
                }
            }

            // Demonstrate arbitrary nesting levels
            try (GroupTestEventReporter outer = root.reportTestGroup("OuterNestingSuite")) {
                outer.started(Instant.now());
                try (GroupTestEventReporter deeper = outer.reportTestGroup("DeeperNestingSuite")) {
                    deeper.started(Instant.now());
                    try (GroupTestEventReporter inner = deeper.reportTestGroup("InnerNestingSuite")) {
                        inner.started(Instant.now());
                        try (TestEventReporter test = inner.reportTest("nestedTest", "nestedTest()")) {
                            test.started(Instant.now());
                            test.succeeded(Instant.now());
                        }
                        inner.succeeded(Instant.now());
                    }
                    deeper.succeeded(Instant.now());
                }
                outer.succeeded(Instant.now());
            }

            if (hasAnyFailures) {
                root.failed(Instant.now());
            } else {
                root.succeeded(Instant.now());
            }
        }
    }
}
