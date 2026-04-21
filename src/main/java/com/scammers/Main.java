package com.scammers;

import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;
import parser.Parser;
import parser.ast.ProgramNode;
import utils.ASTPrettyPrinter;
import utils.ASTDotGenerator;

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

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--ast-format": format = args[++i]; break;
                case "--output-file": outputFile = args[++i]; break;
                case "--verbose": verbose = true; break;
                case "--input": inputFile = args[++i]; break;
                default:
                    if (!args[i].startsWith("--")) inputFile = args[i];
            }
        }

        if (inputFile == null) {
            System.err.println("Usage: java Main <source_file> [--ast-format text|dot|json] [--output-file <file>] [--verbose]");
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
                tokens.forEach(tok -> System.err.println("  " + tok));
                System.err.println("=== Parsing ===");
            }

            Parser parser = new Parser(tokens);
            ProgramNode program = parser.parse();

            if (!parser.getErrors().isEmpty()) {
                System.err.println("Parse errors:");
                parser.getErrors().forEach(e -> System.err.println("  " + e));
            }

            String output = "./";
            switch (format) {
                case "dot":
                    output = new ASTDotGenerator().generate(program);
                    break;
                case "json":
                    System.out.println("Not supported yet");
                    break;
                default:
                    output = new ASTPrettyPrinter().print(program);
                    break;
            }

            if (outputFile != null) {
                Files.writeString(Paths.get(outputFile), output);
                if (verbose) System.err.println("Output written to " + outputFile);
            } else {
                System.out.println(output);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}