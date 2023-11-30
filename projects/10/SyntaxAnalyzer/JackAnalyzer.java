package SyntaxAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class JackAnalyzer {
    private static String fileName;
    private static JackTokenizer tokenizer;

    public static void translateFile(File filepath) {

        try {
            PrintWriter writer = new PrintWriter(filepath.getName().replace(".jack", "Test.xml"));
            writer.println("<tokens>");
            while (tokenizer.hasMoreTokens()) {
                tokenizer.advance();
                String token = tokenizer.getToken();
                if (token.equals(">") || token.equals("<") || token.equals("&")) {
                    token = token.replace("&", "&amp;");
                    token = token.replace(">", "&gt;");
                    token = token.replace("<", "&lt;");
                }
                String tokenType = tokenizer.tokenType();
                writer.println("<" + tokenType + "> " + token + " </" + tokenType + ">");
            }
            writer.println("</tokens>");
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // CompilationEngine compilationEngine = new CompilationEngine(new
        // File("projects\\\\10\\\\ArrayTest\\\\Main.jack"));
        // compilationEngine.CompileClass();
    }

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
                        tokenizer = new JackTokenizer(f);
                        translateFile(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        else {
            try {
                tokenizer = new JackTokenizer(file);
                translateFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
