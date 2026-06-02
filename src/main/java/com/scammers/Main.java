package com.scammers;

import codegen.X86Generator;
import ir.IRGenerator;
import ir.IROptimizer;
import ir.IRProgram;
import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;
import parser.Parser;
import parser.ast.ProgramNode;
import semantic.SemanticAnalyzer;
import utils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        String inputFile = null;
        String outputFile = null;
        String format = "text";
        boolean verbose = false;
        boolean showTypes = false;
        boolean showSymbols = false;
        boolean showReport = false;
        boolean generateIR = false;
        String irFormat = "text";
        String irOutput = null;
        boolean generateAsm = false;
        String asmOutput = null;

        boolean assemblyOnly = false;
        boolean objectOnly = false;
        boolean showHelp = false;
        boolean showVersion = false;
        int optimizationLevel = 1;
        String target = "x86_64";
        List<String> libraries = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--ast-format": format = args[++i]; break;
                case "--output-file": outputFile = args[++i]; break;
                case "--verbose":
                case "-v": verbose = true; break;
                case "--input": inputFile = args[++i]; break;
                case "--show-types": showTypes = true; break;
                case "--show-symbols": showSymbols = true; break;
                case "--show-report": showReport = true; break;
                case "--ir": generateIR = true; break;
                case "--ir-format": irFormat = args[++i]; break;
                case "--ir-output": irOutput = args[++i]; break;
                case "--asm": generateAsm = true; break;
                case "--asm-output": asmOutput = args[++i]; break;
                case "--ast": outputFile = null; break;
                case "-S": assemblyOnly = true; generateAsm = true; break;
                case "-c": objectOnly = true; generateAsm = true; break;
                case "-o": outputFile = args[++i]; break;
                case "--target": target = args[++i]; break;
                case "--help":
                case "-h": showHelp = true; break;
                case "--version": showVersion = true; break;
                case "--optimize": optimizationLevel = 1; break;
                case "-O0": optimizationLevel = 0; break;
                case "-O1": optimizationLevel = 1; break;
                case "-O2": optimizationLevel = 2; break;
                case "-O3": optimizationLevel = 3; break;
                default:
                    if (args[i].startsWith("-l")) {
                        libraries.add(args[i]);
                    } else if (!args[i].startsWith("--")) {
                        inputFile = args[i];
                    }
            }
        }

        if (showHelp) {
            printHelp();
            return;
        }

        if (showVersion) {
            printVersion();
            return;
        }

        if (inputFile == null) {
            System.err.println("Usage: ./mycc [options] <source_file>");
            System.exit(1);
        }

        if (!target.equals("x86_64")) {
            System.err.println("Error: unsupported target: " + target);
            System.exit(1);
        }

        if (assemblyOnly && objectOnly) {
            System.err.println("Error: options -S and -c cannot be used together");
            System.exit(1);
        }

        try {
            String source = Files.readString(Paths.get(inputFile));

            Scanner scanner = new Scanner(source);
            List<Token> tokens = new ArrayList<>();
            Token t;
            do {
                t = scanner.next_token();
                tokens.add(t);
            } while (t.type != TokenType.EOF);

            Parser parser = new Parser(tokens);
            ProgramNode program = parser.parse();

            if (!parser.getErrors().isEmpty()) {
                System.err.println("--- Syntax Errors ---");
                for (String err : parser.getErrors()) {
                    System.err.println("  " + err);
                }
                System.exit(1);
            }

            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyze(program);

            if (!analyzer.getErrors().isEmpty()) {
                System.err.println("--- Semantic Errors ---");
                for (Object err : analyzer.getErrors()) {
                    System.err.println(err.toString());
                }
                System.exit(1);
            }

            IRGenerator irGen = new IRGenerator();
            IRProgram irProgram = irGen.generate(program);

            if (optimizationLevel > 0) {
                IROptimizer optimizer = new IROptimizer(irProgram);
                optimizer.optimize();
            }

            if (showReport) {
                System.out.println(ValidationReport.generate(program, analyzer));
                return;
            } else if (showTypes) {
                System.out.println(new TypeAnnotatedPrinter().print(program));
                return;
            } else if (showSymbols) {
                System.out.println(analyzer.getSymbolTable().dump());
                return;
            }

            if (generateIR) {
                String irResult = irFormat.equals("dot")
                        ? new IRDotGenerator().generate(irProgram)
                        : irProgram.toString();

                if (irOutput != null) {
                    Files.writeString(Paths.get(irOutput), irResult);
                } else {
                    System.out.println(irResult);
                    return;
                }
            }

            if (!generateAsm && outputFile != null && !assemblyOnly && !objectOnly) {
                compileToExecutable(inputFile, outputFile, irProgram, libraries, verbose);
                return;
            }

            if (generateAsm || assemblyOnly || objectOnly) {
                X86Generator x86Gen = new X86Generator();
                String assembly = x86Gen.generate(irProgram);
                String asmPath = getAsmPath(asmOutput, outputFile, assemblyOnly);
                Files.writeString(Paths.get(asmPath), assembly);

                if (verbose) {
                    System.err.println("Assembly written to " + asmPath);
                }

                if (assemblyOnly || generateAsm && !objectOnly) {
                    return;
                }

                String objectPath = outputFile != null ? outputFile : "output.o";
                assemble(asmPath, objectPath, verbose);

                if (objectOnly) {
                    return;
                }
            }

            if (outputFile != null) {
                String astOutput = format.equals("dot")
                        ? new ASTDotGenerator().generate(program)
                        : new ASTPrettyPrinter().print(program);
                Files.writeString(Paths.get(outputFile), astOutput);
            } else {
                String astOutput;
                switch (format) {
                    case "dot": astOutput = new ASTDotGenerator().generate(program); break;
                    case "json": astOutput = "JSON not supported yet"; break;
                    default: astOutput = new ASTPrettyPrinter().print(program); break;
                }
                System.out.println(astOutput);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Error: interrupted");
            System.exit(1);
        }
    }

    private static void compileToExecutable(String inputFile, String outputFile, IRProgram irProgram,
                                            List<String> libraries, boolean verbose)
            throws IOException, InterruptedException {
        X86Generator x86Gen = new X86Generator();
        String assembly = x86Gen.generate(irProgram);
        String asmPath = "out.asm";
        String objectPath = "output.o";
        String runtimeObjectPath = "runtime.o";

        Files.writeString(Paths.get(asmPath), assembly);

        if (verbose) {
            System.err.println("Assembly written to " + asmPath);
        }

        assemble(asmPath, objectPath, verbose);
        assemble("src/main/java/runtime/runtime.asm", runtimeObjectPath, verbose);
        link(objectPath, runtimeObjectPath, outputFile, libraries, verbose);
    }

    private static String getAsmPath(String asmOutput, String outputFile, boolean assemblyOnly) {
        if (asmOutput != null) return asmOutput;
        if (assemblyOnly && outputFile != null) return outputFile;
        return "out.asm";
    }

    private static void assemble(String asmPath, String objectPath, boolean verbose)
            throws IOException, InterruptedException {
        runCommand(Arrays.asList(
                "nasm",
                "-f",
                "elf64",
                asmPath,
                "-o",
                objectPath
        ), verbose);
    }

    private static void link(String objectPath, String runtimeObjectPath, String outputFile,
                             List<String> libraries, boolean verbose)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();

        command.add("gcc");
        command.add("-no-pie");
        command.add(objectPath);
        command.add(runtimeObjectPath);
        command.add("-o");
        command.add(outputFile);

        if (!libraries.contains("-lm")) {
            command.add("-lm");
        }

        command.addAll(libraries);

        runCommand(command, verbose);
    }

    private static void runCommand(List<String> command, boolean verbose)
            throws IOException, InterruptedException {
        if (verbose) {
            System.err.println(String.join(" ", command));
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("command failed with exit code " + exitCode);
        }
    }

    private static void printHelp() {
        System.out.println("Usage: ./mycc [options] <source_file>");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -o <file>          Place output into <file>");
        System.out.println("  -S                 Generate assembly only");
        System.out.println("  -c                 Compile to object file only");
        System.out.println("  -O0|-O1|-O2|-O3    Set optimization level");
        System.out.println("  --ast              Output AST");
        System.out.println("  --ir               Output IR");
        System.out.println("  --target <arch>    Target architecture");
        System.out.println("  -l<lib>            Link with library");
        System.out.println("  -v, --verbose      Verbose output");
        System.out.println("  -h, --help         Display help");
        System.out.println("  --version          Display version");
    }

    private static void printVersion() {
        System.out.println("mycc " + VERSION);
        System.out.println("Compiler for MiniLang");
        System.out.println("Target: x86_64-linux-gnu");
    }
}