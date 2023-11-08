package projects;

import java.io.File;
import java.util.ArrayList;

public class VMTranslator {
    public static void main(String[] args) {
        File fileName = new File(args[0]);
        File fileOut;
        ArrayList<File> files = new ArrayList<>();
        if (args.length != 1)
            throw new IllegalArgumentException(
                    "Inaccurate usage. Please enter in the following format: java VMTranslator (directory/filename)");
        else if (fileName.isFile() && !(args[0].endsWith(".vm")))
            throw new IllegalArgumentException(
                    "Not the correct file type. Please enter a .vm file or a directory containing .vm files. ");
        else {
            if (fileName.isFile() && args[0].endsWith(".vm")) {
                files.add(fileName);
                String firstPart = args[0].substring(0, args[0].length() - 3);
                fileOut = new File(firstPart + ".asm");
            } else // fileName is a directory - access all files in the directory
            {
                files = getVMFiles(fileName);
                fileOut = new File(fileName + ".asm");

            }
        }
        CodeWriter codeWriter = new CodeWriter(fileOut);
        codeWriter.writeInit();
        for (File file : files) {
            codeWriter.setFileName(file);
            Parser parser = new Parser(file);
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
                    case C_LABEL:
                        codeWriter.writeLabel(parser.arg1());
                        break;
                    case C_GOTO:
                        codeWriter.writeGoto(parser.arg1());
                        break;
                    case C_IF:
                        codeWriter.writeIf(parser.arg1());
                        break;
                    case C_FUNCTION:
                        codeWriter.writeFunction(parser.arg1(), parser.arg2());
                        break;
                    case C_RETURN:
                        codeWriter.writeReturn();
                        break;
                    case C_CALL:
                        codeWriter.writeCall(parser.arg1(), parser.arg2());
                    default:
                        break;
                }
            }
        }

        codeWriter.close();

    }

    public static ArrayList<File> getVMFiles(File directory) {
        File[] files = directory.listFiles();
        ArrayList<File> fResults = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".vm"))
                    fResults.add(file);
            }
        }
        return fResults;

    }
}
