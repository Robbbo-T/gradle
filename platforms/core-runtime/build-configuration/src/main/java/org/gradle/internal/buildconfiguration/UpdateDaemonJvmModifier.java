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

package org.gradle.internal.buildconfiguration;

import org.gradle.api.JavaVersion;
import org.gradle.api.UncheckedIOException;
import org.gradle.internal.jvm.inspection.JvmVendor;
import org.gradle.internal.util.PropertiesUtils;
import org.gradle.jvm.toolchain.JvmImplementation;
import org.gradle.util.internal.GFileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class UpdateDaemonJvmModifier {
    public static void updateJvmCriteria(
        File propertiesFile,
        JavaVersion toolchainVersion,
        @Nullable JvmVendor toolchainVendor,
        @Nullable JvmImplementation toolchainImplementation
    ) {
        validateToolchainVersion(toolchainVersion);

        Properties buildProperties = new Properties();
        buildProperties.put(BuildPropertiesDefaults.TOOLCHAIN_VERSION_PROPERTY, toolchainVersion.getMajorVersion());
        if (toolchainVendor != null) {
            buildProperties.put(BuildPropertiesDefaults.TOOLCHAIN_VENDOR_PROPERTY, toolchainVendor.getKnownVendor().name());
        } else {
            buildProperties.remove(BuildPropertiesDefaults.TOOLCHAIN_VENDOR_PROPERTY);
        }
        if (toolchainImplementation != null) {
            buildProperties.put(BuildPropertiesDefaults.TOOLCHAIN_IMPLEMENTATION_PROPERTY, toolchainImplementation.toString());
        } else {
            buildProperties.remove(BuildPropertiesDefaults.TOOLCHAIN_IMPLEMENTATION_PROPERTY);
        }

        GFileUtils.parentMkdirs(propertiesFile);
        try {
            PropertiesUtils.store(buildProperties, propertiesFile, "This file is generated by " + BuildPropertiesPlugin.TASK_NAME);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void validateToolchainVersion(JavaVersion version) {
        // TODO: It would be nice to enforce this as part of task configuration instead of at runtime.
        // TODO: Need to consider how to handle future versions of Java that are not yet known. This currently allows any version of Java above the minimum.
        JavaVersion minimumSupportedVersion = JavaVersion.VERSION_1_8;
        if (version.compareTo(minimumSupportedVersion) < 0) {
            String exceptionMessage = String.format("Unsupported Java version '%s' provided for the 'toolchain-version' option. Gradle can only run with Java %s and above.",
                version.getMajorVersion(), minimumSupportedVersion.getMajorVersion());
            throw new IllegalArgumentException(exceptionMessage);
        }
    }
}
