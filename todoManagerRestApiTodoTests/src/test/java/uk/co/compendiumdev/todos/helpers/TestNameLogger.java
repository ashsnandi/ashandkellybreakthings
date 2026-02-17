package uk.co.compendiumdev.todos.helpers;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestNameLogger implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        String className = context.getRequiredTestClass().getSimpleName();
        String methodName = context.getRequiredTestMethod().getName();
        System.out.println("  >> Running: " + className + "." + methodName);
    }
}
