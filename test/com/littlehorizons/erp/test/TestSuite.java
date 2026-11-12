package com.littlehorizons.erp.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the registered test cases and reports a plain-text summary.
 */
public final class TestSuite {

    private final List<String> names = new ArrayList<>();
    private final List<Assert.ThrowingRunnable> cases = new ArrayList<>();

    public void add(String name, Assert.ThrowingRunnable testCase) {
        names.add(name);
        cases.add(testCase);
    }

    public boolean run() {
        int failures = 0;
        for (int i = 0; i < cases.size(); i++) {
            String name = names.get(i);
            try {
                cases.get(i).run();
                System.out.println("  PASS  " + name);
            } catch (Throwable error) {
                failures++;
                System.out.println("  FAIL  " + name);
                System.out.println("        " + error);
            }
        }
        System.out.println();
        System.out.println(failures == 0
                ? "All " + cases.size() + " tests passed."
                : failures + " of " + cases.size() + " tests failed.");
        return failures == 0;
    }
}
