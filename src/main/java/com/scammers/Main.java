package com.scammers;

import codegen.X86Generator;
import ir.IRGenerator;
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
import java.util.List;

public class Main {
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

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--ast-format": format = args[++i]; break;
                case "--output-file": outputFile = args[++i]; break;
                case "--verbose": verbose = true; break;
                case "--input": inputFile = args[++i]; break;
                case "--show-types": showTypes = true; break;
                case "--show-symbols": showSymbols = true; break;
                case "--show-report": showReport = true; break;
                case "--ir": generateIR = true; break;
                case "--ir-format": irFormat = args[++i]; break;
                case "--ir-output": irOutput = args[++i]; break;
                case "--asm": generateAsm = true; break;
                case "--asm-output": asmOutput = args[++i]; break;
                default:
                    if (!args[i].startsWith("--")) inputFile = args[i];
            }
        }

        if (inputFile == null) {
            System.err.println("Usage: java Main <source_file> [options]");
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

            if (verbose) {
                System.err.println("=== Tokens ===");
                for (Token token : tokens) {
                    System.err.println("  " + token);
                }
            }

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

            if (generateAsm) {
                X86Generator x86Gen = new X86Generator();
                String assembly = x86Gen.generate(irProgram);
                String path = (asmOutput != null) ? asmOutput : "out.asm";
                Files.writeString(Paths.get(path), assembly);
            }

            if (generateIR && irOutput != null) {
                String irResult = irFormat.equals("dot")
                        ? new IRDotGenerator().generate(irProgram)
                        : irProgram.toString();
                Files.writeString(Paths.get(irOutput), irResult);
            }

            if (outputFile != null) {
                String astOutput = format.equals("dot")
                        ? new ASTDotGenerator().generate(program)
                        : new ASTPrettyPrinter().print(program);
                Files.writeString(Paths.get(outputFile), astOutput);
            }

            if (showReport) {
                System.out.println(ValidationReport.generate(program, analyzer));
            } else if (showTypes) {
                System.out.println(new TypeAnnotatedPrinter().print(program));
            } else if (generateIR && irOutput == null) {
                System.out.println(irProgram.toString());
            } else if (showSymbols) {
                System.out.println(analyzer.getSymbolTable().dump());
            } else if (outputFile == null) {
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
        }
    }
}