package SyntaxAnalyzer;

import java.io.File;
import java.io.IOException;

public class JackAnalyzer {
    

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java JackAnalyzer <file.jack|dir>");
            System.exit(1);
        }

        File file = new File(args[0]);

        if (file.isDirectory())
            for (File f : file.listFiles()) {
                if (f.getName().endsWith(".jack")) {
                    try {
                        // Run tokenizer
                        JackTokenizer tokenizer = new JackTokenizer(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        else {
            try {
                JackTokenizer tokenizer = new JackTokenizer(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
