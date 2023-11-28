package SyntaxAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class JackAnalyzer {
    private static String fileName;
    private static JackTokenizer tokenizer;

    public static void translateFile() {


        while (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
            String token = tokenizer.getToken();
            System.out.println(token);
        }

        
    }

    public static void main(String[] args) {
        // if (args.length != 1) {
        //     System.out.println("Usage: java JackAnalyzer <file.jack|dir>");
        //     System.exit(1);
        // }

        // File file = new File(args[0]);

        // if (file.isDirectory())
        //     for (File f : file.listFiles()) {
        //         if (f.getName().endsWith(".jack")) {
        //             try {
        //                 // Run tokenizer
        //                 JackTokenizer tokenizer = new JackTokenizer(f);
        //             } catch (IOException e) {
        //                 e.printStackTrace();
        //             }
        //         }
        //     }
        // else {
        //     try {
        //         JackTokenizer tokenizer = new JackTokenizer(file);

        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }
        
        try {
            tokenizer = new JackTokenizer(new File("projects\\10\\ArrayTest\\Main.jack"));
            translateFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
