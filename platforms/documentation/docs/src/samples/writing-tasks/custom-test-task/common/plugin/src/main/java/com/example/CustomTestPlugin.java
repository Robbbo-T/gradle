package com.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class CustomTestPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("test", CustomTest.class, task -> {
            task.setGroup("verification");
            task.setDescription("Runs the tests.");
            task.getFail().convention(false);
        });
    }
}
