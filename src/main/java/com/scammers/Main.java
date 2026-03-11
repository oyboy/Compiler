package com.scammers;

import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <source_file>");
            System.exit(1);
        }

        try {
            String source = Files.readString(Paths.get(args[0]));
            Scanner scanner = new Scanner(source);

            while (true) {
                Token token = scanner.next_token();
                System.out.println(token);
                if (token.type == TokenType.EOF) break;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}