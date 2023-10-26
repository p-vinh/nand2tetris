import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class VMTranslator {
    private File file;
    private Parser parser;
    private CodeWriter codeWriter;

    public VMTranslator(File file) {
        this.file = file;
        this.parser = new Parser(file);
        this.codeWriter = new CodeWriter(file);
    }

    public void generateAssemblyFile() {
        if (file.isDirectory()) {
            iterateFiles(file.listFiles());
        } else {
            translate(file);
        }
        codeWriter.close();
    }

    private void translate(File file) {


        while (parser.hasMoreCommands()) {
            parser.advance();
            switch (parser.commandType()) {
                case C_PUSH:
                case C_POP:
                    codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                    break;
                case C_ARITHMETIC:
                    codeWriter.writeArithmetic(parser.arg1());
                    break;
                default:
                    break;
            }
        }
    }

    private void iterateFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                iterateFiles(file.listFiles()); // Calls same method again.
            } else {
                if (file.getName().endsWith(".vm")) {
                    translate(file);
                }
            }
        }
    }

    public static void main(String[] args) {
        File inputFile = new File("projects\\07\\StackArithmetic\\StackTest\\StackTest.vm");
        VMTranslator vmTranslator = new VMTranslator(inputFile);
        vmTranslator.generateAssemblyFile();
    }
}