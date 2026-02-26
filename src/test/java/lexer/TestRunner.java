package lexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestRunner {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    private static final String ROOT_PATH = "src/test/java/lexer/";

    public static void main(String[] args) {
        System.out.println("Running Scanner Test Suite...");

        int passed = 0;
        int failed = 0;

        System.out.println("Current directory: " + System.getProperty("user.dir"));
        passed += runTestsInDir(new File(ROOT_PATH + "valid"));
        passed += runTestsInDir(new File(ROOT_PATH + "invalid"));

        System.out.println("\n--------------------------------------------------");
        System.out.println("TOTAL: " + (passed + failed));
        System.out.println(ANSI_GREEN + "PASSED: " + passed + ANSI_RESET);
        if (failed > 0) {
            System.out.println(ANSI_RED + "FAILED: " + failed + ANSI_RESET);
            System.exit(1);
        }
    }

    private static int runTestsInDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return 0;
        int count = 0;
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".src")) {
                if (runSingleTest(file)) {
                    count++;
                } else {
                    System.exit(1);
                }
            }
        }
        return count;
    }

    private static boolean runSingleTest(File srcFile) {
        String testName = srcFile.getName();
        File expectedFile = new File(srcFile.getParent(), testName.replace(".src", ".txt"));

        System.out.print("Testing " + testName + "... ");

        try {
            String source = Files.readString(srcFile.toPath());
            Scanner scanner = new Scanner(source);
            StringBuilder actualOutput = new StringBuilder();

            while (true) {
                Token t = scanner.next_token();
                actualOutput.append(t.toString()).append("\n");
                if (t.type == TokenType.EOF) break;
            }

            if (!expectedFile.exists()) {
                System.out.println(ANSI_RED + "MISSING out FILE" + ANSI_RESET);
                return false;
            }

            String expectedOutput = Files.readString(expectedFile.toPath());

            String actual = actualOutput.toString().trim().replace("\r\n", "\n");
            String expected = expectedOutput.trim().replace("\r\n", "\n");

            if (actual.equals(expected)) {
                System.out.println(ANSI_GREEN + "OK" + ANSI_RESET);
                return true;
            } else {
                System.out.println(ANSI_RED + "FAIL" + ANSI_RESET);
                System.out.println("Expected:\n" + expected);
                System.out.println("Actual:\n" + actual);
                return false;
            }

        } catch (IOException e) {
            System.out.println(ANSI_RED + "ERROR: " + e.getMessage() + ANSI_RESET);
            return false;
        }
    }
}