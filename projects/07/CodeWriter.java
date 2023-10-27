import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;

public class CodeWriter {
    private PrintWriter writer;
    private int labelCounter = 0;
    private String file;
    private Map<String, String> segmentMap = new HashMap<String, String>();
    private Map<String, String> arithmeticMap = new HashMap<String, String>();

    public CodeWriter(File file) {
        try {
            File outputFile = new File(file.getAbsolutePath().split(".vm")[0] + ".asm");
            writer = new PrintWriter(new FileWriter(outputFile));
            this.file = file.getName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        segmentMap.put("local", "@LCL");
        segmentMap.put("argument", "@ARG");
        segmentMap.put("this", "@THIS");
        segmentMap.put("that", "@THAT");
        segmentMap.put("pointer", "@3");
        segmentMap.put("temp", "@5");
        segmentMap.put("static", "@16");
        segmentMap.put("constant", "@");

        // Arithmetic
        arithmeticMap.put("add", "M=M+D");
        arithmeticMap.put("sub", "M=M-D");
        arithmeticMap.put("neg", "M=-M");
        arithmeticMap.put("eq", "D;JEQ");
        arithmeticMap.put("gt", "D;JGT");
        arithmeticMap.put("lt", "D;JLT");
        arithmeticMap.put("and", "M=D&M");
        arithmeticMap.put("or", "M=D|M");
        arithmeticMap.put("not", "M=!M");
    }

    public void setFileName(String fileName) {
        file = fileName;
    }

    public void writeArithmetic(String command) {
        try {
            switch(command) {
                case "add":
                    writer.println(formatArithLogic().append(arithmeticMap.get(command)).toString());
                    break;
                case "sub":
                    writer.println(formatArithLogic().append(arithmeticMap.get(command)).toString());
                    break;
                case "and":
                    writer.println(formatArithLogic().append(arithmeticMap.get(command)).toString());
                    break;
                case "or":
                    writer.println(formatArithLogic().append(arithmeticMap.get(command)).toString());
                    break;
                case "not":
                    writer.println("@SP\nA=M-1\nM=!M");
                    break;
                case "eq":
                    writer.println(formatArithLogic().append(arithmeticMap.get(command)).toString());
                    break;
                case "gt":
                    writer.println(formatArithLogic().append(arithmeticMap.get(command)).toString());
                    break;
                case "lt":
                    writer.println(formatArithLogic().append(arithmeticMap.get(command)).toString());
                    break;
                case "neg":
                    writer.println("@SP\nA=M-1\nM=-M");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writePushPop(Parser.CommandType commandType, String segment, int index) {
        try {
            if (commandType == Parser.CommandType.C_PUSH) {
                if (segment.equals("constant")) {
                    writer.println(segmentMap.get(segment) + index);
                    writer.println("D=A");
                } else if (segment.equals("static")) {
                    writer.println(segmentMap.get(segment) + "." + index);
                    writer.println("D=M");
                } else {
                    writer.println(segmentMap.get(segment));
                    writer.println("D=M");
                    writer.println("@" + index);
                    writer.println("A=D+A");
                    writer.println("D=M");
                }
                // Push D onto stack
                writer.println("@SP");
                writer.println("A=M"); // Get address of SP
                writer.println("M=D"); // Set value of D to address of SP
                // Increment SP pointer by 1
                writer.println("@SP");
                writer.println("M=M+1");
            } else if (commandType == Parser.CommandType.C_POP) {
                if (segment.equals("static")) {
                    writer.println("@SP");
                    writer.println("M=M-1");
                    writer.println("A=M");
                    writer.println("D=M");
                    writer.println(segmentMap.get(segment) + "." + index);
                    writer.println("M=D");
                } else {
                    writer.println(segmentMap.get(segment));
                }

                writer.println("@SP");
                writer.println("A=M");
                writer.println("D=M"); // Set D to value of SP
                writer.println("@SP");
                writer.println("M=M-1"); // Decrement SP pointer by 1 (Popping value off stack)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringBuilder formatArithLogic() {
        StringBuilder sb = new StringBuilder();
        sb.append("@SP\n"); // Get address of SP
        sb.append("AM=M-1\n"); // Decrement A and M by 1; A is now address of SP; M is now value of SP
        sb.append("D=M\n"); // Set D to value of SP (Get first value)
        sb.append("A=A-1\n"); // Decrement A by 1 (Get address of second value)
        return sb;
    }
    
    // Closes the output file
    public void close() {
        writer.close();
    }
}
