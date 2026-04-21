package tests;

import java.io.File;
import java.nio.file.Files;

public abstract class BaseTestRunner {
    static final String GREEN = "\u001B[32m";
    static final String RED = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String RESET = "\u001B[0m";

    protected int passed = 0;
    protected int failed = 0;

    protected abstract String getActualOutput(String source, boolean isErrorTest) throws Exception;

    protected abstract String getExpectedExtension();

    protected abstract String getSuiteName();

    public void run(String... testDirs) {
        System.out.println("=== " + getSuiteName() + " ===\n");

        for (String dir : testDirs) {
            runDir(new File(dir));
        }

        System.out.println("\n=== Results ===");
        System.out.println("TOTAL: " + (passed + failed));
        System.out.println(GREEN + "PASSED: " + passed + RESET);
        if (failed > 0) {
            System.out.println(RED + "FAILED: " + failed + RESET);
            System.exit(1);
        } else {
            System.out.println(GREEN + "ALL TESTS PASSED" + RESET);
        }
    }

    private void runDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println(YELLOW + "SKIP: " + dir.getPath() + " (not found)" + RESET);
            return;
        }

        System.out.println("--- " + dir.getPath() + " ---");

        File[] srcFiles = dir.listFiles((d, name) -> name.endsWith(".src"));
        if (srcFiles == null || srcFiles.length == 0) {
            System.out.println(YELLOW + "  (no .src files)" + RESET);
            return;
        }

        for (File srcFile : srcFiles) {
            runTest(srcFile);
        }
    }

    private void runTest(File srcFile) {
        String testName = srcFile.getName();
        String ext = getExpectedExtension();
        File expectedFile = new File(srcFile.getParent(), testName.replace(".src", ext));
        boolean isErrorTest = srcFile.getAbsolutePath().contains("invalid");

        System.out.print("  " + testName + " ... ");

        try {
            String source = Files.readString(srcFile.toPath());
            String actual = getActualOutput(source, isErrorTest);

            if (actual == null) {
                failed++;
                return;
            }

            actual = actual.trim().replace("\r\n", "\n");

            if (!expectedFile.exists()) {
                Files.writeString(expectedFile.toPath(), actual);
                System.out.println(YELLOW + "CREATED " + ext + RESET);
                passed++;
                return;
            }

            String expected = Files.readString(expectedFile.toPath()).trim().replace("\r\n", "\n");

            if (actual.equals(expected)) {
                System.out.println(GREEN + "OK" + RESET);
                passed++;
            } else {
                System.out.println(RED + "FAIL" + RESET);
                failed++;
                printDiff(expected, actual);
            }

        } catch (Exception e) {
            System.out.println(RED + "ERROR: " + e.getMessage() + RESET);
            failed++;
        }
    }

    private void printDiff(String expected, String actual) {
        String[] expLines = expected.split("\n");
        String[] actLines = actual.split("\n");
        int maxLines = Math.max(expLines.length, actLines.length);

        for (int i = 0; i < maxLines; i++) {
            String exp = i < expLines.length ? expLines[i] : "<missing>";
            String act = i < actLines.length ? actLines[i] : "<missing>";
            if (!exp.equals(act)) {
                System.out.println("    Line " + (i + 1) + ":");
                System.out.println("      Expected: " + exp);
                System.out.println("      Actual:   " + act);
            }
        }
    }
}
